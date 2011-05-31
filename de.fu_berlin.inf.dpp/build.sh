#! /bin/sh

# convenience wrapper to include external libraries into ant calls
# ft 20100503

# expects to be called in the Saros project directory

if [ $# -eq 0 ]
then
	echo "Usage: $0 [OPTIONAL ANT ARGS] TARGET [TARGET]..."
	echo
	echo "Set path to externally used libraries via environment variables:"
	echo "COBERTURA_ROOT, ANT4ECLIPSE_ROOT, ECLIPSE_DIR"
	exit 1
fi

VM_ARGS_DEFAULT="-Xms512m -Xmx1024m -XX:MaxPermSize=256m"

: ${ANT_OPTS:="$VM_ARGS_DEFAULT"}

export ANT_OPTS

# Building feature requires feature version,
# assuming it's the same as in the plugin manifest
PLUGIN_VERSION=`grep Bundle-Version META-INF/MANIFEST.MF | cut -d' ' -f2`

ant\
 -lib $ANT4ECLIPSE_ROOT/libs\
 -lib $ANT4ECLIPSE_ROOT\
 -Dcobertura.dir=$COBERTURA_ROOT\
 -Dant4eclipse.dir=$ANT4ECLIPSE_ROOT\
 -Dfeature.version=$PLUGIN_VERSION\
 -Declipse.plugin.dir=$ECLIPSE_DIR/plugins $@
 