import sys
import xml.etree.ElementTree as ET
from typing import Optional

XML_FILE_PATH = "sherlock-branding/resources/idea/SherlockPlatformApplicationInfo.xml"
NAMESPACE = {'appinfo': 'http://jetbrains.org/intellij/schema/application-info'}


def get_version_from_xml() -> Optional[str]:
  """
  Reads the SherlockPlatformApplicationInfo.xml file, extracts the version,
  and returns it in 'v1.0.0' format.
  """
  try:
    tree = ET.parse(XML_FILE_PATH)
    root = tree.getroot()
    version_element = root.find('appinfo:version', NAMESPACE)
    if version_element is not None:
      major = version_element.get('major')
      minor = version_element.get('minor')
      patch = version_element.get('patch')
      if major is not None and minor is not None:
        patch_str = patch if patch is not None else "0"
        return f"v{major}.{minor}.{patch_str}"

  except ET.ParseError as e:
    print(f"Error parsing XML file: {e}", file=sys.stderr)

  except FileNotFoundError:
    print(f"Error: XML file not found at {XML_FILE_PATH}", file=sys.stderr)

  except Exception as e:
    print(f"An unexpected error occurred: {e}", file=sys.stderr)

  return None


if __name__ == "__main__":
  version = get_version_from_xml()

  if version:
    print(f"VERSION={version}")
  else:
    sys.exit(1)
