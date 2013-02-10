#!/bin/sh

version="1.5"
arch="i386"
qtversion="4.5.2_01" 


package_dir=$(cd `dirname $0` && pwd)

destination="$package_dir/contents/usr/share/nixnote"
source_dir="../.."
qtlibs="../../../bitrock/lib"
qtarch="32"

if [ "$(id -u)" != "0" ]; then
   echo "This script must be run as root" 1>&2
   exit 1
fi


#Do any parameter overrides
while [ -n "$*" ]
do
   eval $1
   shift
done


if [ "$arch" = "amd64" ]; then
  qtarch="64"
fi


# Cleanup any old stuff

if [ -e "$package_dir/nixnote-${version}_${arch}.deb" ] 
then
   rm $package_dir/nixnote-${version}_${arch}.deb
fi

if [ -e "$package_dir/nixnote-${version}_${arch}.rpm" ] 
then
   rm $package_dir/nixnote-${version}_${arch}.rpm
fi

############################
# Copy the things we need  #
############################

# Create directories
mkdir $package_dir/contents
mkdir $package_dir/contents/usr/
mkdir $package_dir/contents/usr/share
mkdir $package_dir/contents/usr/share/applications
mkdir $package_dir/contents/usr/share/nixnote
mkdir $package_dir/contents/usr/share/man
mkdir $package_dir/contents/usr/share/man/man1
mkdir $package_dir/contents/usr/bin/
mkdir $package_dir/contents/usr/share/doc
mkdir $package_dir/contents/usr/share/doc/nixnote

# Copy startup script & images
cp $source_dir/nixnote.sh $package_dir/contents/usr/share/nixnote/
cp $source_dir/*.txt $package_dir/contents/usr/share/nixnote/
cp $source_dir/*.html $package_dir/contents/usr/share/nixnote/
cp $source_dir/*.png $package_dir/contents/usr/share/nixnote/
cp $source_dir/nixnote.desktop $package_dir/contents/usr/share/applications
cp $source_dir/nixnote_path.sh $package_dir/contents/usr/bin/nixnote
cp $source_dir/copyright $package_dir/contents/usr/share/doc/nixnote
gzip -c -9 $source_dir/changelog.txt > $package_dir/contents/usr/share/doc/nixnote/changelog.gz
gzip -c -9 $source_dir/changelog.txt > $package_dir/contents/usr/share/doc/nixnote/changelog.Debian.gz


# Copy subdirectories
cp -r $source_dir/images $package_dir/contents/usr/share/nixnote/
cp -r $source_dir/lib $package_dir/contents/usr/share/nixnote/
cp -r $source_dir/qss $package_dir/contents/usr/share/nixnote/
cp -r $source_dir/spell $package_dir/contents/usr/share/nixnote/
cp -r $source_dir/translations $package_dir/contents/usr/share/nixnote/
cp -r $source_dir/xml $package_dir/contents/usr/share/nixnote/


# Copy QT libraries.
cp $qtlibs/qtjambi-linux$qtarch-$qtversion.jar $package_dir/contents/usr/share/nixnote/lib/
cp $qtlibs/qtjambi-linux$qtarch-gcc-$qtversion.jar $package_dir/contents/usr/share/nixnote/lib/

# Copy NixNote itself
cp $qtlibs/../nixnote.jar $package_dir/contents/usr/share/nixnote/

# Reset user permissions
chown -R root:root $package_dir/contents/


# Copy control file for the package
mkdir $package_dir/contents/DEBIAN
cp $package_dir/$arch/control ./contents/DEBIAN/

# Gzip the man page
gzip -c -9 $source_dir/man/nixnote.1 > $package_dir/contents/usr/share/man/man1/nixnote.1.gz

dpkg -b $package_dir/contents $package_dir/nixnote-${version}_${arch}.deb
alien -r $package_dir/nixnote-${version}_${arch}.deb

# Cleanup
rm -rf $package_dir/contents

# Check package
#lintian $package_dir/contents $package_dir/nixnote-${version}_${arch}.deb
