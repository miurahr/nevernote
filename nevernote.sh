#! /bin/sh

###########################################
# Location variables.  Edit the variables #
# below to your specific installation.    #
# The ones below are examples only.       #
###########################################
NEVERNOTE=$HOME/NeverNote
JAMBI_LOCATION=$HOME/qtjambi
JAMBI_VERSION=4.5.2_01
JAMBI_PLATFORM=linux32-gcc
JAVA_LIB_DIR=/usr/share/lib

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
NN_XMS=256M
# Maximum heap size
NN_XMX=512M

## The young generation
# the young generation will occupy 1/2 of total heap
NN_NEW_RATIO=1

## GC option
## recommend Incremental Low Pause GC for desktop apps 
NN_GC_OPT=-Xincgc
## recent multi-core CPU may show good performance
#NN_GC_OPT=-XX:+UseParNewGC
#NN_GC_OPT=-XX:+UseConcMarkSweepGC
## same as default
#NN_GC_OPT=-XX:+UseParallelGC

## debug
#NN_DEBUG=-agentlib:hprof=format=b
#NN_DEBUG=-agentlib:hprof=cpu=samples,format=a
#NN_DEBUG=-verbose:gc 

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
#NN_NAME="sandbox"  


###################################################################
###################################################################
## You probably don't need to change anything below this line.   ##
###################################################################
###################################################################


#####################
# Setup environment #
#####################
NN_CLASSPATH=$NEVERNOTE/nevernote.jar
NN_CLASSPATH=$NN_CLASSPATH:$NEVERNOTE/lib/evernote.jar
NN_CLASSPATH=$NN_CLASSPATH:$NEVERNOTE/lib/h2-1.2.136.jar
NN_CLASSPATH=$NN_CLASSPATH:$NEVERNOTE/lib/libthrift.jar
NN_CLASSPATH=$NN_CLASSPATH:$NEVERNOTE/lib/log4j-1.2.14.jar
NN_CLASSPATH=$NN_CLASSPATH:$NEVERNOTE/lib/PDFRenderer.jar
NN_CLASSPATH=$NN_CLASSPATH:$NEVERNOTE/lib/commons-lang-2.4.jar
NN_CLASSPATH=$NN_CLASSPATH:$NEVERNOTE/lib/jtidy-r938.jar 
NN_CLASSPATH=$NN_CLASSPATH:$JAMBI_LOCATION/qtjambi-$JAMBI_VERSION.jar
NN_CLASSPATH=$NN_CLASSPATH:$JAMBI_LOCATION/qtjambi-util-$JAMBI_VERSION.jar
NN_CLASSPATH=$NN_CLASSPATH:$JAMBI_LOCATION/qtjambi-$JAMBI_PLATFORM-$JAMBI_VERSION.jar


###################
# Run the program #
###################
cd $NEVERNOTE
java -Xmx$NN_XMX -Xms$NN_XMS -XX:NewRatio=$NN_NEW_RATIO $NN_GC_OPT $NN_DEBUG -classpath $NN_CLASSPATH cx.fbn.nevernote.NeverNote --name=$NN_NAME
cd -
