#!/bin/sh

package_dir=$(cd `dirname $0` && pwd)

if [ "$(id -u)" != "0" ]; then
   echo "This script must be run as root" 1>&2
   exit 1
fi

cp $package_dir/usr/share/applications/nixnote.desktop /usr/share/applications/nixnote.desktop
mkdir /usr/share/nixnote
cp -r $package_dir/usr/share/nixnote/* /usr/share/nixnote/

echo "Install complete"
