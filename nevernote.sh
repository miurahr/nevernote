l#! /bin/sh

###########################################
# Location variables.  Edit the variables #
# below to your specific installation.    #
# The ones below are examples only.       #
###########################################
export NEVERNOTE=/home/randy/NeverNote
export JAMBI_LOCATION=/home/randy/qtjambi
export JAMBI_VERSION=4.5.2_01
export JAMBI_PLATFORM=linux32-gcc


########################################
# Memory settings.  These can be tuned #
# to your specific needs.  The greater #
# the memory allocated the better      #
# your response may be, but the more   #
# resources the program will consume.  #
# Lower numbers may hurt performance   #
# but will reduce resource held by     #
# the program.  If you get errors      #
# that say "out of memory" you need    #
# to increase these values.            #
########################################
# Initial heap size
export NN_XMS=128M
# Maximum heap size
export NN_XMX=512M


########################################
# This next variable is optional. It   #
# is only needed if you want to run    #
# multiple copies of NeverNote under   #
# the same Linux user id.  Each        #
# additional copy (after the first)    #
# should have a unique name.  This     #
# permits the settings to be saved     #
# properly.  If you only want to run   #
# one copy under a single userid, this #
# can be commented out.                #
########################################
#export NN_NAME="sandbox"  


###################################################################
###################################################################
## You probably don't need to change anything below this line.   ##
###################################################################
###################################################################


#####################
# Setup environment #
#####################
export NN_CLASSPATH=$NEVERNOTE/nevernote.jar
export NN_CLASSPATH=$NN_CLASSPATH:$NEVERNOTE/lib/evernote.jar
export NN_CLASSPATH=$NN_CLASSPATH:$NEVERNOTE/lib/h2-1.2.136.jar
export NN_CLASSPATH=$NN_CLASSPATH:$NEVERNOTE/lib/libthrift.jar
export NN_CLASSPATH=$NN_CLASSPATH:$NEVERNOTE/lib/log4j-1.2.14.jar
export NN_CLASSPATH=$NN_CLASSPATH:$NEVERNOTE/lib/PDFRenderer.jar
export NN_CLASSPATH=$NN_CLASSPATH:$JAMBI_LOCATION/qtjambi-$JAMBI_VERSION.jar
export NN_CLASSPATH=$NN_CLASSPATH:$JAMBI_LOCATION/qtjambi-$JAMBI_PLATFORM-$JAMBI_VERSION.jar


###################
# Run the program #
###################
cd $NEVERNOTE
java -Xmx$NN_XMX -Xms$NN_XMS -classpath $NN_CLASSPATH cx.fbn.nevernote.NeverNote --name=$NN_NAME
cd -
