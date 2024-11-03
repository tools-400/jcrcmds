# JCRCMDS

## Description

The JCRCMDS plug-in is a wrapper plug-in for the JCRCMDS utility of Craig Rutledge. The JCRCMDS command converts fixed format H, F and D specifications into free format DCL-* statements.

Craig Rutledge: http://www.jcrcmds.com/

RDi 9.5.1.3+ update site: https://tools-400.github.io/jcrcmds/update-site/eclipse/rdi8.0/

Further information about iRPGUnit are available on the [Jcrcmds Web Site](https://tools-400.github.io/jcrcmds/).

## Usage

1) Load a RPG source member into the Lpex editor.
2) Right-click into the source member to open the context menu.
3) Select "JCRCMDS -> Convert all" or "JCRCMDS -> Convert selection" to start the conversion.

Then the plug-in uploads the entire source member or the selected lines to the host, executes command JCRHFD, downloads the converted source lines and updates the source member.