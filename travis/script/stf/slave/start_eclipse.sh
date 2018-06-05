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

host_ip=`/sbin/ifconfig eth0 | grep "inet addr" | cut -d ":" -f 2 | cut -d " " -f 1`

eclipse_dir="/eclipse"
eclipse_plugin_dir="${eclipse_dir}/plugins"
eclipse_dropin_dir="${eclipse_dir}/dropins"

workspace="${STF_WS}/ws/workspace_${user}"

plugin_id_prefix="de.fu_berlin.inf.dpp"
saros_plugin_dir="${STF_WS}/plugins"

# determine (versioned) filename of plugin (_ suppresses .source versions)
saros_plugin_filename=`ls -1 $saros_plugin_dir | grep "${plugin_id_prefix}_[0-9]*"`

if [ ! -d "$STF_WS/e_plugins" ]; then
  mkdir $STF_WS/e_plugins
  cp -r $eclipse_plugin_dir/* $STF_WS/e_plugins/
fi

if [ -z $saros_plugin_filename ]; then
  echo "cannot find Saros plugin in $saros_plugin_dir"
  exit 1
fi

echo "deleting workspace: ${workspace}"
rm -rf "${workspace}"

if [ ! -e "${saros_plugin_dir}/.lock" ]; then
  touch "${saros_plugin_dir}/.lock"
  echo "deleting old plugin(s)"
  rm -f "${eclipse_dropin_dir}/de.fu_berlin.inf"*
  echo "installing plugins via dropin directory"
  cp "${saros_plugin_dir}/de.fu_berlin.inf"* "${eclipse_dropin_dir}"
fi

mkdir -p "${workspace}"
mkdir -p "${workspace}/.metadata/.plugins/org.eclipse.core.runtime/.settings"

# enable auto close of eclipse

echo EXIT_PROMPT_ON_CLOSE_LAST_WINDOW=false > "${workspace}/.metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.ui.ide.prefs"

# keep SVN quite
echo ask_user_for_usage_report_preference=false > "${workspace}/.metadata/.plugins/org.eclipse.core.runtime/.settings/org.tigris.subversion.subclipse.tools.usage.prefs"

printf "grant{\npermission java.security.AllPermission;\n};" > "${workspace}/stf.policy"


# get path to equinox jar inside eclipse home folder

CLASSPATH=$(find "${eclipse_plugin_dir}" -name "org.eclipse.equinox.launcher_*.jar" | sort | tail -1);

CLASSPATH="${CLASSPATH}:${COBERTURA_HOME}/cobertura.jar"

LD_LIBRARY_PATH=/usr/lib/jni:${LD_LIBRARY_PATH}

export LD_LIBRARY_PATH
export DISPLAY
export CLASSPATH

echo "starting Eclipse for user ${user}"

$java_cmd -version

# be sure to set -Dosgi.parentClassloader=app otherwise instrumented classes would throw a class not found exception

echo $LD_LIBRARY_PATH
echo $DISPLAY
echo $CLASSPATH

$java_cmd \
  -XX:MaxPermSize=192m -Xms384m -Xmx512m -ea \
  -Djava.rmi.server.codebase="file:${saros_plugin_filename}" \
  -Djava.security.manager \
  -Djava.security.policy="file:${workspace}/stf.policy" \
  -Djava.rmi.server.hostname="${host_ip}" \
  -Dde.fu_berlin.inf.dpp.debug=true \
  -Dde.fu_berlin.inf.dpp.testmode="${rmi_port}" \
  -Dde.fu_berlin.inf.dpp.sleepTime=200 \
  -Dorg.eclipse.swtbot.keyboard.strategy=org.eclipse.swtbot.swt.finder.keyboard.MockKeyboardStrategy \
  -Dorg.eclipse.swtbot.keyboard.layout=de.fu_berlin.inf.dpp.stf.server.bot.default \
  -Dfile.encoding=UTF-8 \
  -Dnet.sourceforge.cobertura.datafile="${workspace}/coverage_${user}.ser" \
  -Dosgi.parentClassloader=app \
  org.eclipse.equinox.launcher.Main \
  -name "eclipse_${user}" \
  -consoleLog \
  -data "${workspace}"

