#!/bin/sh

package_dir=$(cd `dirname $0` && pwd)

if [ "$(id -u)" != "0" ]; then
   echo "This script must be run as root" 1>&2
   exit 1
fi

cp $package_dir/usr/share/applications/nevernote.desktop /usr/share/applications/nevernote.desktop
mkdir /usr/share/nevernote
cp -r $package_dir/usr/share/nevernote/* /usr/share/nevernote/

echo "Install complete"
