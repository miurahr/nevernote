#!/bin/sh

version="1.5"
arch="i386"
qtversion="4.5.2_01" 


package_dir=$(cd `dirname $0` && pwd)

destination="$package_dir/nixnote/usr/share/nixnote"
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

if [ -e "$package_dir/nixnote-${version}_${arch}.tar.gz" ] 
then
   rm $package_dir/nixnote-${version}_${arch}.tar.gz
fi

############################
# Copy the things we need  #
############################

# Create directories
mkdir $package_dir/nixnote
mkdir $package_dir/nixnote/usr/
mkdir $package_dir/nixnote/usr/share
mkdir $package_dir/nixnote/usr/share/applications
mkdir $package_dir/nixnote/usr/share/nixnote
mkdir $package_dir/nixnote/usr/share/man
mkdir $package_dir/nixnote/usr/bin/


# Copy startup script & images
cp $source_dir/install.sh $package_dir/nixnote/
cp $source_dir/*.sh $package_dir/nixnote/usr/share/nixnote/
cp $source_dir/*.txt $package_dir/nixnote/usr/share/nixnote/
cp $source_dir/*.html $package_dir/nixnote/usr/share/nixnote/
cp $source_dir/*.png $package_dir/nixnote/usr/share/nixnote/
cp $source_dir/nixnote.desktop $package_dir/nixnote/usr/share/applications
cp $source_dir/nixnote_path.sh $package_dir/nixnote/usr/bin/nixnote.sh

# Copy subdirectories
cp -r $source_dir/images $package_dir/nixnote/usr/share/nixnote/
cp -r $source_dir/lib $package_dir/nixnote/usr/share/nixnote/
cp -r $source_dir/qss $package_dir/nixnote/usr/share/nixnote/
cp -r $source_dir/spell $package_dir/nixnote/usr/share/nixnote/
cp -r $source_dir/translations $package_dir/nixnote/usr/share/nixnote/
cp -r $source_dir/xml $package_dir/nixnote/usr/share/nixnote/


# Copy QT libraries.
cp $qtlibs/qtjambi-linux$qtarch-$qtversion.jar $package_dir/nixnote/usr/share/nixnote/lib/
cp $qtlibs/qtjambi-linux$qtarch-gcc-$qtversion.jar $package_dir/nixnote/usr/share/nixnote/lib/

# Copy NixNote itself
cp $qtlibs/../nixnote.jar $package_dir/nixnote/usr/share/nixnote/

# Reset user permissions
chown -R root:root $package_dir/nixnote/

cd $package_dir
tar -czf $package_dir/nixnote-${version}_${arch}.tar.gz ./nixnote
cd -

# Cleanup
rm -rf $package_dir/nixnote

