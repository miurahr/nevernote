#!/bin/sh

version="0.92.1"
arch="i386"
qtversion="4.5.2_01" 


package_dir=$(cd `dirname $0` && pwd)

destination="$package_dir/nevernote/usr/share/nevernote"
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

if [ -e "$package_dir/nevernote-${version}_${arch}.tar.gz" ] 
then
   rm $package_dir/nevernote-${version}_${arch}.tar.gz
fi

############################
# Copy the things we need  #
############################

# Create directories
mkdir $package_dir/nevernote
mkdir $package_dir/nevernote/usr/
mkdir $package_dir/nevernote/usr/share
mkdir $package_dir/nevernote/usr/share/applications
mkdir $package_dir/nevernote/usr/share/nevernote

# Copy startup script & images
cp $source_dir/install.sh $package_dir/nevernote/
cp $source_dir/*.sh $package_dir/nevernote/usr/share/nevernote/
cp $source_dir/*.txt $package_dir/nevernote/usr/share/nevernote/
cp $source_dir/*.html $package_dir/nevernote/usr/share/nevernote/
cp $source_dir/*.png $package_dir/nevernote/usr/share/nevernote/
cp $source_dir/nevernote.desktop $package_dir/nevernote/usr/share/applications

# Copy subdirectories
cp -r $source_dir/images $package_dir/nevernote/usr/share/nevernote/
cp -r $source_dir/lib $package_dir/nevernote/usr/share/nevernote/
cp -r $source_dir/qss $package_dir/nevernote/usr/share/nevernote/
cp -r $source_dir/spell $package_dir/nevernote/usr/share/nevernote/
cp -r $source_dir/translations $package_dir/nevernote/usr/share/nevernote/
cp -r $source_dir/xml $package_dir/nevernote/usr/share/nevernote/


# Copy QT libraries.
cp $qtlibs/qtjambi-linux$qtarch-$qtversion.jar $package_dir/nevernote/usr/share/nevernote/lib/
cp $qtlibs/qtjambi-linux$qtarch-gcc-$qtversion.jar $package_dir/nevernote/usr/share/nevernote/lib/

# Copy NeverNote itself
cp $qtlibs/../nevernote.jar $package_dir/nevernote/usr/share/nevernote/

# Reset user permissions
chown -R root:root $package_dir/nevernote/

cd $package_dir
tar -czf $package_dir/nevernote-${version}_${arch}.tar.gz ./nevernote
cd -

# Cleanup
rm -rf $package_dir/nevernote

