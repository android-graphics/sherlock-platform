// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.platform.ijent.community.impl.nio

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.platform.ijent.IjentId
import com.intellij.platform.ijent.IjentSessionRegistry
import com.intellij.platform.ijent.community.impl.IjentFsResultImpl
import com.intellij.platform.ijent.community.impl.nio.IjentNioFileSystem.FsAndUserApi
import com.intellij.platform.ijent.community.impl.nio.IjentNioFileSystemProvider.UnixFilePermissionBranch.*
import com.intellij.platform.ijent.fs.*
import com.intellij.platform.ijent.fs.IjentFileInfo.Type.*
import com.intellij.platform.ijent.fs.IjentFileSystemPosixApi.*
import com.intellij.platform.ijent.fs.IjentPosixFileInfo.Type.Symlink
import kotlinx.coroutines.job
import java.io.IOException
import java.net.URI
import java.nio.channels.AsynchronousFileChannel
import java.nio.channels.FileChannel
import java.nio.channels.SeekableByteChannel
import java.nio.file.*
import java.nio.file.StandardOpenOption.*
import java.nio.file.attribute.*
import java.nio.file.spi.FileSystemProvider
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

class IjentNioFileSystemProvider : FileSystemProvider() {
  companion object {
    @JvmStatic
    fun getInstance(): IjentNioFileSystemProvider =
      installedProviders()
        .filterIsInstance<IjentNioFileSystemProvider>()
        .single()
  }

  private val registeredFileSystems = ConcurrentHashMap<IjentId, IjentNioFileSystem>()

  override fun getScheme(): String = "ijent"

  override fun newFileSystem(uri: URI, env: MutableMap<String, *>?): IjentNioFileSystem {
    typicalUriChecks(uri)

    if (!uri.path.isNullOrEmpty()) {
      TODO("Filesystems with non-empty paths are not supported yet.")
    }

    val ijentId = IjentId(uri.host)

    val ijentApi = IjentSessionRegistry.instance().ijents[ijentId]
    require(ijentApi != null) {
      "$ijentApi is not registered in ${IjentSessionRegistry::class.java.simpleName}"
    }
    val ijentFsAndUser = FsAndUserApi.create(ijentApi)

    val fs = IjentNioFileSystem(this, ijentFsAndUser, onClose = { registeredFileSystems.remove(ijentId) })

    if (registeredFileSystems.putIfAbsent(ijentId, fs) != null) {
      throw FileSystemAlreadyExistsException("A filesystem for $ijentId is already registered")
    }

    fs.ijent.fs.coroutineScope.coroutineContext.job.invokeOnCompletion {
      registeredFileSystems.remove(ijentId)
    }

    return fs
  }

  override fun newFileSystem(path: Path, env: MutableMap<String, *>?): IjentNioFileSystem =
    newFileSystem(path.toUri(), env)

  override fun getFileSystem(uri: URI): IjentNioFileSystem {
    typicalUriChecks(uri)
    return registeredFileSystems[IjentId(uri.host)] ?: throw FileSystemNotFoundException()
  }

  override fun getPath(uri: URI): IjentNioPath =
    getFileSystem(uri).run {
      getPath(
        when (ijent) {
          is FsAndUserApi.Posix -> uri.path
          is FsAndUserApi.Windows -> uri.path.trimStart('/')
        }
      )
    }

  override fun newByteChannel(path: Path, options: Set<OpenOption>, vararg attrs: FileAttribute<*>): SeekableByteChannel =
    newFileChannel(path, options, *attrs)

  override fun newFileChannel(path: Path, options: Set<OpenOption>, vararg attrs: FileAttribute<*>?): FileChannel {
    ensureIjentNioPath(path)
    require(path.ijentPath is IjentPath.Absolute)
    // TODO Handle options and attrs
    val fs = registeredFileSystems[path.ijentId] ?: throw FileSystemNotFoundException()

    require(!(READ in options && APPEND in options)) { "READ + APPEND not allowed" }
    require(!(APPEND in options && TRUNCATE_EXISTING in options)) { "APPEND + TRUNCATE_EXISTING not allowed" }

    return if (WRITE in options || APPEND in options) {
      if (DELETE_ON_CLOSE in options) TODO("WRITE + CREATE_NEW")
      if (LinkOption.NOFOLLOW_LINKS in options) TODO("WRITE + NOFOLLOW_LINKS")

      val writeOptions = fs.ijent.fs
        .writeOptionsBuilder(path.ijentPath)
        .append(APPEND in options)
        .truncateExisting(TRUNCATE_EXISTING in options)
        .creationMode(when {
          CREATE_NEW in options -> IjentFileSystemApi.FileWriterCreationMode.ONLY_CREATE
          CREATE in options -> IjentFileSystemApi.FileWriterCreationMode.ALLOW_CREATE
          else -> IjentFileSystemApi.FileWriterCreationMode.ONLY_OPEN_EXISTING
        }).build()


      fs.fsBlocking {
        if (READ in options) {
          IjentNioFileChannel.createReadingWriting(fs, writeOptions)
        } else {
          IjentNioFileChannel.createWriting(fs, writeOptions)
        }
      }
    }
    else {
      if (CREATE in options) TODO("READ + CREATE")
      if (CREATE_NEW in options) TODO("READ + CREATE_NEW")
      if (DELETE_ON_CLOSE in options) TODO("READ + CREATE_NEW")
      if (LinkOption.NOFOLLOW_LINKS in options) TODO("READ + NOFOLLOW_LINKS")

      fs.fsBlocking {
        IjentNioFileChannel.createReading(fs, path.ijentPath)
      }
    }
  }

  override fun newDirectoryStream(dir: Path, pathFilter: DirectoryStream.Filter<in Path>?): DirectoryStream<Path> {
    ensureIjentNioPath(dir)
    val nioFs = dir.nioFs

    return nioFs.fsBlocking {
      // TODO listDirectoryWithAttrs+sun.nio.fs.BasicFileAttributesHolder
      val childrenNames = nioFs.ijent.fs.listDirectory(ensurePathIsAbsolute(dir.ijentPath)).getOrThrowFileSystemException()

      val nioPathList = childrenNames.asSequence()
        .map { childName ->
          IjentNioPath(dir.ijentPath.getChild(childName).getOrThrow(), nioFs)
        }
        .filter { nioPath ->
          pathFilter?.accept(nioPath) != false
        }
        .toMutableList()

      object : DirectoryStream<Path> {
        // The compiler doesn't (didn't?) allow to relax types here.
        override fun iterator(): MutableIterator<Path> = nioPathList.iterator()
        override fun close(): Unit = Unit
      }
    }
  }

  override fun createDirectory(dir: Path, vararg attrs: FileAttribute<*>?) {
    ensureIjentNioPath(dir)
    val path = dir.ijentPath
    try {
      ensurePathIsAbsolute(path)
    } catch (e : IllegalArgumentException) {
      throw IOException(e)
    }
    try {
      dir.nioFs.fsBlocking {
        when (val fsApi = dir.nioFs.ijent.fs) {
          is IjentFileSystemPosixApi -> fsApi.createDirectory(path, emptyList())
          is IjentFileSystemWindowsApi -> TODO()
        }
      }
    }
    catch (e : CreateDirectoryException) {
      when (e) {
        is CreateDirectoryException.DirAlreadyExists,
        is CreateDirectoryException.FileAlreadyExists -> throw FileAlreadyExistsException(dir.toString())
        is CreateDirectoryException.ParentNotFound -> throw NoSuchFileException(dir.toString(), null, "Parent directory not found")
        else -> throw IOException(e)
      }
    }
  }

  override fun delete(path: Path) {
    ensureIjentNioPath(path)
    path.nioFs.fsBlocking {
      try {
        path.nioFs.ijent.fs.deleteDirectory(path.ijentPath as IjentPath.Absolute, false)
      } catch (e : IjentFileSystemApi.DeleteException.DirNotEmpty) {
        val exception = DirectoryNotEmptyException(path.toString())
        exception.addSuppressed(e)
        throw exception
      }
    }
  }

  override fun copy(source: Path, target: Path, vararg options: CopyOption) {
    if (StandardCopyOption.ATOMIC_MOVE in options) {
      throw UnsupportedOperationException("Unsupported copy option")
    }
    ensureIjentNioPath(source)
    ensureIjentNioPath(target)
    val sourcePath = source.ijentPath
    val targetPath = target.ijentPath
    ensurePathIsAbsolute(sourcePath)
    ensurePathIsAbsolute(targetPath)
    source.nioFs.fsBlocking {
      var builder = source.nioFs.ijent.fs.copyOptionsBuilder(sourcePath, targetPath)
      for (option in options) {
        builder = when (option) {
          StandardCopyOption.REPLACE_EXISTING -> builder.replaceExisting()
          StandardCopyOption.COPY_ATTRIBUTES -> builder.copyAttributes()
          StandardCopyOption.ATOMIC_MOVE -> builder.atomicMove()
          else -> {
            thisLogger().warn("Unknown copy option: $option. This option will be ignored.")
            builder
          }
        }
      }
      source.nioFs.ijent.fs.copy(builder.build())
    }
  }

  override fun move(source: Path, target: Path, vararg options: CopyOption?) {
    TODO("Not yet implemented")
  }

  override fun isSameFile(path: Path, path2: Path): Boolean {
    ensureIjentNioPath(path)
    ensureIjentNioPath(path2)
    val nioFs = path.nioFs

    return nioFs
      .fsBlocking {
        nioFs.ijent.fs.sameFile(ensurePathIsAbsolute(path.ijentPath), ensurePathIsAbsolute(path2.ijentPath))
      }
      .getOrThrowFileSystemException()
  }

  override fun isHidden(path: Path): Boolean {
    TODO("Not yet implemented")
  }

  override fun getFileStore(path: Path): FileStore =
    IjentNioFileStore(ensureIjentNioPath(path).nioFs.ijent.fs)

  private enum class UnixFilePermissionBranch { OWNER, GROUP, OTHER }

  override fun checkAccess(path: Path, vararg modes: AccessMode) {
    val fs = ensureIjentNioPath(path).nioFs
    fs.fsBlocking {
      when (val ijent = fs.ijent) {
        is FsAndUserApi.Posix -> {
          // According to the javadoc, this method must follow symlinks.
          val fileInfo = ijent.fs.stat(ensurePathIsAbsolute(path.ijentPath), resolveSymlinks = true).getOrThrowFileSystemException()
          // Inspired by sun.nio.fs.UnixFileSystemProvider#checkAccess
          val filePermissionBranch = when {
            ijent.userInfo.uid == fileInfo.permissions.owner -> OWNER
            ijent.userInfo.gid == fileInfo.permissions.group -> GROUP
            else -> OTHER
          }

          if (AccessMode.READ in modes) {
            val canRead = when (filePermissionBranch) {
              OWNER -> fileInfo.permissions.ownerCanRead
              GROUP -> fileInfo.permissions.groupCanRead
              OTHER -> fileInfo.permissions.otherCanRead
            }
            if (!canRead) {
              (IjentFsResultImpl.PermissionDenied(path.ijentPath, "Permission denied: read") as IjentFsError).throwFileSystemException()
            }
          }
          if (AccessMode.WRITE in modes) {
            val canWrite = when (filePermissionBranch) {
              OWNER -> fileInfo.permissions.ownerCanWrite
              GROUP -> fileInfo.permissions.groupCanWrite
              OTHER -> fileInfo.permissions.otherCanWrite
            }
            if (!canWrite) {
              (IjentFsResultImpl.PermissionDenied(path.ijentPath, "Permission denied: write") as IjentFsError).throwFileSystemException()
            }
          }
          if (AccessMode.EXECUTE in modes) {
            val canExecute = when (filePermissionBranch) {
              OWNER -> fileInfo.permissions.ownerCanExecute
              GROUP -> fileInfo.permissions.groupCanExecute
              OTHER -> fileInfo.permissions.otherCanExecute
            }
            if (!canExecute) {
              (IjentFsResultImpl.PermissionDenied(path.ijentPath, "Permission denied: execute") as IjentFsError).throwFileSystemException()
            }
          }
        }
        is FsAndUserApi.Windows -> TODO()
      }
    }
  }

  override fun <V : FileAttributeView?> getFileAttributeView(path: Path, type: Class<V>?, vararg options: LinkOption?): V {
    TODO("Not yet implemented")
  }

  override fun <A : BasicFileAttributes> readAttributes(path: Path, type: Class<A>, vararg options: LinkOption): A {
    val fs = ensureIjentNioPath(path).nioFs

    val result = when (fs.ijent) {
      is FsAndUserApi.Posix ->
        IjentNioPosixFileAttributes(fs.fsBlocking {
          statPosix(path.ijentPath, fs.ijent.fs, LinkOption.NOFOLLOW_LINKS in options)
        })

      is FsAndUserApi.Windows -> TODO()
    }

    @Suppress("UNCHECKED_CAST")
    return result as A
  }

  private tailrec suspend fun statPosix(path: IjentPath, fsApi: IjentFileSystemPosixApi, resolveSymlinks: Boolean): IjentPosixFileInfo {
    val stat = fsApi.stat(ensurePathIsAbsolute(path), resolveSymlinks = resolveSymlinks).getOrThrowFileSystemException()
    return when (val type = stat.type) {
      is Directory, is Other, is Regular, is Symlink.Unresolved -> stat
      is Symlink.Resolved -> statPosix(type.result, fsApi, resolveSymlinks)
    }
  }

  override fun readAttributes(path: Path, attributes: String, vararg options: LinkOption): MutableMap<String, Any> {
    TODO("Not yet implemented")
  }

  override fun setAttribute(path: Path, attribute: String?, value: Any?, vararg options: LinkOption?) {
    TODO("Not yet implemented")
  }

  override fun newAsynchronousFileChannel(
    path: Path?,
    options: MutableSet<out OpenOption>?,
    executor: ExecutorService?,
    vararg attrs: FileAttribute<*>?,
  ): AsynchronousFileChannel {
    TODO("Not yet implemented")
  }

  override fun createSymbolicLink(link: Path?, target: Path?, vararg attrs: FileAttribute<*>?) {
    TODO("Not yet implemented")
  }

  override fun createLink(link: Path?, existing: Path?) {
    TODO("Not yet implemented")
  }

  override fun readSymbolicLink(link: Path?): Path {
    TODO("Not yet implemented")
  }

  @OptIn(ExperimentalContracts::class)
  private fun ensureIjentNioPath(path: Path): IjentNioPath {
    contract {
      returns() implies (path is IjentNioPath)
    }

    if (path !is IjentNioPath) {
      throw ProviderMismatchException("$path (${path.javaClass}) is not ${IjentNioPath::class.java.simpleName}")
    }

    return path
  }

  @OptIn(ExperimentalContracts::class)
  private fun ensurePathIsAbsolute(path: IjentPath): IjentPath.Absolute {
    contract {
      returns() implies (path is IjentPath.Absolute)
    }

    return when (path) {
      is IjentPath.Absolute -> path
      is IjentPath.Relative -> throw InvalidPathException(path.toString(), "Relative paths are not accepted here")
    }
  }

  private fun typicalUriChecks(uri: URI) {
    require(uri.host.isNotEmpty())

    require(uri.scheme == scheme)
    require(uri.userInfo.isNullOrEmpty())
    require(uri.query.isNullOrEmpty())
    require(uri.fragment.isNullOrEmpty())
  }
}