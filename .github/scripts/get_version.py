import sys
import xml.etree.ElementTree as ET

XML_FILE_PATH = "sherlock-branding/resources/idea/SherlockPlatformApplicationInfo.xml"
NAMESPACE_URI = "http://jetbrains.org/intellij/schema/application-info"


def get_version_from_xml() -> str:
  """
  Reads the SherlockPlatformApplicationInfo.xml file, extracts the version,
  and returns it in 'v1.0.0' format.
  """
  try:
    tree = ET.parse(XML_FILE_PATH)
    root = tree.getroot()
    version_element = root.find(f'{{{NAMESPACE_URI}}}version')
    if version_element is not None:
      major = version_element.get("major")
      minor = version_element.get("minor")
      patch = version_element.get("patch", default='0')
      if major is not None and minor is not None:
        return f"{major}.{minor}.{patch}"

  except ET.ParseError as e:
    print(f"Error parsing XML file: {e}", file=sys.stderr)
    raise

  except FileNotFoundError:
    print(f"Error: XML file not found at {XML_FILE_PATH}", file=sys.stderr)
    raise

  except Exception as e:
    print(f"An unexpected error occurred: {e}", file=sys.stderr)
    raise


if __name__ == "__main__":
  try:
    version = get_version_from_xml()
    print(version)

  except Exception as e:
    print(f"Error: {e}")
    sys.exit(1)
