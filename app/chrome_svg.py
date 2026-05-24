import urllib.request
import re
try:
    svg = urllib.request.urlopen("https://upload.wikimedia.org/wikipedia/commons/e/e1/Google_Chrome_icon_%28February_2022%29.svg").read().decode('utf-8')
    # print out the paths
    for p in re.findall(r'<path[^>]*d="([^"]+)"', svg):
        print("PATH: ", p)
except Exception as e:
    print(e)
