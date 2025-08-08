# Sherlock Platform

## About

This is the customized IntelliJ platform for Sherlock Project based
off the IntelliJ Platform Version 2024.2. To read more on the IntelliJ Platform documentation scroll down to the IntelliJ IDEA Community
Edition section.

## Download Instructions

* Download your OS specific released version of Sherlock Platform from the released tags.
* _**On MacOS:**_ To give permissions to run-

1. Check if this flag is set:

```
xattr -p com.apple.quarantine /path/to/app/Sherlock\ Platform.app
```

2. And delete:

```
xattr -d com.apple.quarantine /path/to/app/Sherlock\ Platform.app
```

* Launch the application.

## Developer Documentation

### Build Instructions for Sherlock Platform

The following steps build Sherlock Platform only on Linux and Mac:

* Run `chmod +x ./build_sherlock_platform.sh` from the root of the directory to grant access.
* Run `./build_sherlock_platform.sh` to build the platform.
* Extract the artifact from `out/sherlock-platform/artifacts` according to the OS.
* Run the platform from `out/sherlock-platform/artifacts/sherlock-platform-242.21829/Sherlock\ Platform-2024.2.1/bin/`

## Creating a New Release for Sherlock Platform

The `main` branch of Sherlock Platform always points to the current pre-release version and the upcoming release version. Releases for the
Sherlock Platform are created using a GitHub Actions workflow. All released artifacts
will be visible on the "Release" page.

**Prerequisites:**

1. **GitHub CLI (`gh`)**: Ensure you have `gh` installed and authenticated. See [cli.github.com](https://cli.github.com/).
2. **Repository Permissions**: You need write access to the repository to trigger workflows and create releases.
3. **Version Updated**: Before triggering the release, ensure that the version number in the
   `sherlock-branding/resources/idea/SherlockPlatformApplicationInfo.xml` file is
   updated to the desired release version on the `main` branch.

**Steps using `gh` CLI:**

1. **Navigate to your local repository directory**.
2. **Trigger the workflow:** Use the `gh workflow run` command.

    ```bash
    gh workflow run release.yml --ref main
    ```

3. **Monitor the Workflow:** After running the command, `gh` will provide a link to track the workflow run's progress on GitHub under the "
   Actions" tab. After a successful run, workflow will create a new release and upload the build artifacts (`.tar.gz`, `.zip`) to the
   release page.

4. **Point `main` to the next release version:** After the successful creation of a release, update the version
   number in the
   `sherlock-branding/resources/idea/SherlockPlatformApplicationInfo.xml` file to the next release version on the `main` branch. (e.g. After
   releasing v1.1, the version in `SherlockPlatformApplicationInfo.xml` should be updated to v1.2 )

**Important:** Unlike manual release processes, you do **not** need to create the Git tag yourself beforehand. The workflow handles tag
creation based on the version it reads.

# IntelliJ IDEA Community Edition [![official JetBrains project](http://jb.gg/badges/official.svg)](https://github.com/JetBrains/.github/blob/main/profile/README.md)

These instructions will help you build IntelliJ IDEA Community Edition from source code, which is the basis for IntelliJ Platform
development.
The following conventions will be used to refer to directories on your machine:

* `<USER_HOME>` is your home directory.
* `<IDEA_HOME>` is the root directory for the IntelliJ source code.

## Getting IntelliJ IDEA Community Edition Source Code

IntelliJ IDEA Community Edition source code is available from `github.com/JetBrains/intellij-community` by either cloning or
downloading a zip file (based on a branch) into `<IDEA_HOME>`. The default is the *master* branch.

The master branch contains the source code which will be used to create the next major version of IntelliJ IDEA. The branch names
and build numbers for older releases of IntelliJ IDEA can be found on the page of
[Build Number Ranges](https://plugins.jetbrains.com/docs/intellij/build-number-ranges.html).

These Git operations can also be done through
the [IntelliJ IDEA user interface](https://www.jetbrains.com/help/idea/using-git-integration.html).

_**Speed Tip:**_ If the complete repository history isn't needed, then using a shallow clone (`git clone --depth 1`) will save significant
time.

_**On Windows:**_ Two git options are required to check out sources on Windows. Since it's a common source of Git issues on Windows anyway,
those options could be set globally (execute those commands before cloning any of intellij-community/android repositories):

* `git config --global core.longpaths true`
* `git config --global core.autocrlf input`

IntelliJ IDEA Community Edition requires additional Android modules from separate Git repositories. To clone these repositories,
run one of the `getPlugins` scripts located in the `<IDEA_HOME>` directory. Use the `--shallow` argument if the complete repository history
isn't needed.
These scripts clone their respective *master* branches. Make sure you are inside the `<IDEA_HOME>` directory when running those scripts, so
the modules get cloned inside the `<IDEA_HOME>` directory.

* `getPlugins.sh` for Linux or macOS.
* `getPlugins.bat` for Windows.

_**Note:**_ Always `git checkout` the `intellij-community` and `android` Git repositories to the same branches/tags.

## Building IntelliJ Community Edition

Version 2023.2 or newer of IntelliJ IDEA Community Edition or IntelliJ IDEA Ultimate Edition is required to build and develop
for the IntelliJ Platform.

### Opening the IntelliJ Source Code for Build

Using IntelliJ IDEA **File | Open**, select the `<IDEA_HOME>` directory.

* If IntelliJ IDEA displays an error about a missing or out of date required plugin (e.g. Kotlin),
  [enable, upgrade, or install that plugin](https://www.jetbrains.com/help/idea/managing-plugins.html) and restart IntelliJ IDEA.

### IntelliJ Build Configuration

1. It's recommended to use JetBrains Runtime 17 to compile the project.
   When you invoke **Build Project** for the first time, IntelliJ IDEA should suggest downloading it automatically.
2. If the _Maven_ plugin is disabled, [add the path variable](https://www.jetbrains.com/help/idea/absolute-path-variables.html)
   "**MAVEN_REPOSITORY**" pointing to `<USER_HOME>/.m2/repository` directory.
3. Make sure you have at least 8GB of RAM on your computer. With the bare minimum of RAM, disable "Compile independent modules in parallel"
   option in [the compiler settings](https://www.jetbrains.com/help/idea/specifying-compilation-settings.html). With notably more memory
   available, increase "User-local build process heap size" to 3000 - that will greatly reduce compilation time.

Note that it is important to use the variant of JetBrains Runtime **without JCEF**.
So, if for some reason `jbr-17` SDK points to an installation of JetBrains Runtime with JCEF, you need to change it:
ensure that IntelliJ IDEA is running in internal mode (by adding `idea.is.internal=true` to `idea.properties` file), navigate to `jbr-17`
item in Project Structure | SDKs, click on 'Browse' button, choose 'Download...' item and select version 17 and vendor 'JetBrains Runtime'.

### Building the IntelliJ Application Source Code

To build IntelliJ IDEA Community Edition from source, choose **Build | Build Project** from the main menu.

To build installation packages, run the `installers.cmd` command in `<IDEA_HOME>` directory. `installers.cmd` will work on both Windows and
Unix systems.

Options to build installers are passed as system properties to `installers.cmd` command.
You may find the list of available properties in [BuildOptions.kt](platform/build-scripts/src/org/jetbrains/intellij/build/BuildOptions.kt)

Examples (`./` should be added only for Linux/macOS):

* Build installers only for current operating system: `./installers.cmd -Dintellij.build.target.os=current`
* Build source code _incrementally_ (do not build what was already built before):
  `./installers.cmd -Dintellij.build.incremental.compilation=true`

`installers.cmd` is used just to run [OpenSourceCommunityInstallersBuildTarget](build/src/OpenSourceCommunityInstallersBuildTarget.kt) from
the command line.
You may call it directly from IDEA, see run configuration `Build IDEA Community Installers (current OS)` for an example.

#### Dockerized Build Environment

To build installation packages inside a Docker container with preinstalled dependencies and tools, run the following command in
`<IDEA_HOME>` directory (on Windows, use PowerShell):
`docker run --rm -it --user "$(id -u)" --volume "${PWD}:/community" "$(docker build --quiet . --target intellij_idea)"`

> Please remember to specify the `--user "$(id -u)"` argument for the container's user to match the host's user. 
> This is required not to affect the permissions of the checked-out repository, the build output and the mounted Maven cache, if any.

To reuse the existing Maven cache from the host system, add the following option to `docker run` command:
`--volume "$HOME/.m2:/home/ide_builder/.m2"`

## Running IntelliJ IDEA

To run the IntelliJ IDEA built from source, choose **Run | Run** from the main menu. This will use the preconfigured run configuration "*
*IDEA**".

To run tests on the build, apply these setting to the **Run | Edit Configurations... | Templates | JUnit** configuration tab:

* Working dir: `<IDEA_HOME>/bin`
* VM options:
  * `-ea`

You can find other helpful information at [https://www.jetbrains.com/opensource/idea](https://www.jetbrains.com/opensource/idea).
The "Contribute Code" section of that site describes how you can contribute to IntelliJ IDEA.

## Running IntelliJ IDEA on CI/CD environment

To run tests outside of IntelliJ IDEA, run the `tests.cmd` command in `<IDEA_HOME>` directory. `tests.cmd` will work on both Windows and
Unix systems.

Options to run tests are passed as system properties to `tests.cmd` command.
You may find the list of available properties
in [TestingOptions.kt](platform/build-scripts/src/org/jetbrains/intellij/build/TestingOptions.kt)

Examples (`./` should be added only for Linux/macOS):

* Build source code _incrementally_ (do not build what was already built before):
  `./tests.cmd -Dintellij.build.incremental.compilation=true`
* Run a specific test: `./tests.cmd -Dintellij.build.test.patterns=com.intellij.util.ArrayUtilTest`

`tests.cmd` is used just to run [CommunityRunTestsBuildTarget](build/src/CommunityRunTestsBuildTarget.kt) from the command line.
You may call it directly from IDEA, see run configuration `tests in community` for an example.
