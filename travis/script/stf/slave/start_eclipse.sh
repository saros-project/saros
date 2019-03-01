#!/bin/bash -e
# Has to be called within a stf_slave container
# params DISPLAY, user(e.g: Alice, Bob), PORT

[ ! -z "$DEBUG_BASH" ] && set -x

DISPLAY=":$1"
user=$2
rmi_port=$3

java_cmd=`/usr/bin/which java`

if [ ! -x $java_cmd ]; then
  echo "error, no java executable found"
  exit 1
fi

host_ip=`/sbin/ifconfig eth0 | grep "inet" | awk '{print $2}'`

STF_WS=/stf_ws
eclipse_dir="/eclipse"
eclipse_plugin_dir="${eclipse_dir}/plugins"
eclipse_dropin_dir="${eclipse_dir}/dropins"

workspace="${STF_WS}/ws/workspace_${user}"

plugin_id_prefix="saros.eclipse"
saros_plugin_dir="${STF_WS}/plugins"

# determine (versioned) filename of plugin (_ suppresses .source versions)
saros_plugin_filename=`ls -1 $saros_plugin_dir | grep "$plugin_id_prefix.jar"`

if [ -z $saros_plugin_filename ]; then
  echo "cannot find Saros plugin in $saros_plugin_dir"
  exit 1
fi


dropins_content=$(ls -1 $eclipse_dropin_dir)
if [ -z "$dropins_content" ]; then
  echo "installing plugins via dropin directory"
  cp -v "${saros_plugin_dir}/saros"* "${eclipse_dropin_dir}"
fi

mkdir -p "${workspace}"
mkdir -p "${workspace}/.metadata/.plugins/org.eclipse.core.runtime/.settings"

# enable auto close of eclipse

echo EXIT_PROMPT_ON_CLOSE_LAST_WINDOW=false > "${workspace}/.metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.ui.ide.prefs"

# keep SVN quite
echo ask_user_for_usage_report_preference=false > "${workspace}/.metadata/.plugins/org.eclipse.core.runtime/.settings/org.tigris.subversion.subclipse.tools.usage.prefs"

printf "grant{\npermission java.security.AllPermission;\n};" > "${workspace}/stf.policy"


# get path to equinox jar inside eclipse home folder

LD_LIBRARY_PATH=/usr/lib/jni:${LD_LIBRARY_PATH}

export LD_LIBRARY_PATH
export DISPLAY
export CLASSPATH

echo "starting Eclipse for user ${user}"

$java_cmd -version

# be sure to set -Dosgi.parentClassloader=app otherwise instrumented classes would throw a class not found exception

echo $LD_LIBRARY_PATH
echo $DISPLAY

$eclipse_dir/eclipse \
  -name "eclipse_${user}" \
  -consoleLog \
  -data "${workspace}" \
  -vm "$java_cmd" \
  -vmargs \
  -XX:MaxPermSize=192m -Xms384m -Xmx512m -ea \
  -Djava.rmi.server.codebase="file:${saros_plugin_filename}" \
  -Djava.security.manager \
  -Djava.security.policy="file:${workspace}/stf.policy" \
  -Djava.rmi.server.hostname="${host_ip}" \
  -Dsaros.debug=true \
  -Dsaros.testmode="${rmi_port}" \
  -Dsaros.sleepTime=200 \
  -Dorg.eclipse.swtbot.keyboard.strategy=org.eclipse.swtbot.swt.finder.keyboard.MockKeyboardStrategy \
  -Dorg.eclipse.swtbot.keyboard.layout=saros.stf.server.bot.default \
  -Dfile.encoding=UTF-8 \
  -Dosgi.parentClassloader=app \

