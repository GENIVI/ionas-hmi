-------------------------------------------------------------------------
                IoNAS AUTOSAR/Franca Integration Tool
-------------------------------------------------------------------------

1. INTRODUCTION

The IoNAS project provides an Eclipse-based tool for the connection
of AUTOSAR/GENIVI systems on model level.

An Artop model (for the AUTOSAR side) and a set of Franca interfaces
(for the GENIVI side) can be linked using a Connector model.
The IoNAS tool will create additional model artifacts which represent
a communication connection between the AUTOSAR part and the GENIVI part
of the overall system.

NB: The actual model transformation is not part of this open-source
project. It is being developed in the confined AUTOSAR space (i.e.,
AUTOSAR Subversion repository), which can only be accessed by 
AUTOSAR Development members.

IoNAS-HMI is provided under Eclipse Public License v1 (EPL v1).


-------------------------------------------------------------------------

2. PRODUCT STRUCTURE

The tool consists of the actual model transformation between AUTOSAR and
Franca models and some additional infrastructure, e.g., some UI functions
for starting the transformation from an Eclipse IDE and monitoring the
transformation's output in the Eclipse console.

The source code for the model transformation itself is not part of this
open-source project due to AUTOSAR legal restrictions. Instead, the
transformation is provided in binary form as Eclipse feature 
org.autosar.ionas.feature. This allows to use the IoNAS tool without
access to the transformation's source code. A license for
this binary distribution is issued by Fraunhofer and will be presented
for review and click-through during the installation process.


-------------------------------------------------------------------------

3. INSTALLATION

This section describes how to install IoNAS using a binary Eclipse
update site provided on projects.genivi.org. If you would like to
actively participate in this open-source project or study the code,
please refer to section "5. FOR DEVELOPERS" below.


3.1 Generic Installation

The installable product comes as an Eclipse P2 repository (which can be
used as standard Eclipse update site). A typical installation can be
accomplished by the following steps:

a) Download Eclipse Luna, edition "Eclipse IDE for Java and DSL
   Developers" from here:
   https://www.eclipse.org/downloads/index-developer.php

b) Install and run it. You have to choose a new workspace folder at 
   startup.

c) Open the Eclipse Preferences dialog, choose "Install / Update" and
   its sub-page "Available Software Sites" and add the following sites:
   - Orbit: http://download.eclipse.org/tools/orbit/downloads/drops/R20140525021250/repository/
   - Franca: http://franca.eclipselabs.org.codespot.com/git/update_site/releases/0.9.0/
   - Sphinx: http://download.eclipse.org/sphinx/updates/releases/0.8.x
   - Artop: https://www.artop.org/containers/artop-sdk-update-site-4.2/

d) Open "Help > Install New Software..." and press "Add...".
   Enter the location of the IoNAS update site:
   - http://docs.projects.genivi.org/ionas-ui-update-site/
   Select the following features:
   - Franca-AUTOSAR Interface Translation
   - Franca-AUTOSAR Interface Translation UI
   Selecting the Source-feature is optional.

e) During the installation process, you will have to accept the license
   agreement for the use of the binary converter plug-in. The license
   is issued by Fraunhofer-Gesellschaft. The license can be reviewed
   in Eclipse's "Review Licenses" dialog.

f) Complete the installation process. The installation will
   automagically download the required packages from the software
   sites specified in step (c).

g) Optional: If you don't have a specific AUTOSAR authoring tool yet,
   you might want to install feature "Autosar 4.1.1 SDK" from the Artop
   site. This will provide you with editors and views for reviewing
   and editing arxml models (e.g., in the AUTOSAR Explorer tree).


3.2 Custom Installation

If your development environment already contains a configured Eclipse IDE
with some of the preliminaries installed, you should try to install the 
IoNAS update site on top. This could be the case if you are already using
an AUTOSAR authoring tool or an installation of Franca with CommonAPI.
You might then need to add some missing dependencies throughout the
process. Please note that we can only test the generic installation as
described above. If you encounter unforeseen problems during
installation, please contact this project's mailing list.


-------------------------------------------------------------------------

4. USAGE

A typical workflow with IoNAS will consist of the following steps:

a) In the workspace, prepare an Eclipse project which will contain
   the input and output models as well as the Connector files.
b) Prepare an AUTOSAR-model (*.arxml, using the Artop implementation)
c) Prepare some Franca interface specifications (*.fidl). In a minimal
   example, Franca interfaces might be generated from an AUTOSAR-model.
   Thus, this step is optional.
d) Write a Connector model (*.fcon). IoNAS provides an easy-to-use,
   text-based editor for this. The editor supports code completion,
   syntax highlighting, jump-to-reference and many other features.
e) In the context menu of the Connector-model file, you can start the
   model transformation by choosing the "IoNAS: Connect Models" action.
   The transformation will be started, some logging information will
   be printed to the IDE's console and the resulting files will be
   created as specified in the Connector model.

Due to the dependencies specified by IoNAS some example AUTOSAR editors 
from the Artop project will be installed which can be used to review 
the input and output AUTOSAR models.

The resulting models (arxml and Franca) can then be used by downstream
tools, e.g., code generators. In the GENIVI space, you might refer
to the INC topic (short for: Inter-Node-Communication) to get some more
information on how the resulting models can be implemented on an actual
target system.


-------------------------------------------------------------------------

5. FOR DEVELOPERS

If you would like to join the open-source project as a developer, you
can get a clone from the projects.genivi.org git repository. Currently,
read-only cloning is available for anybody via

   http://git.projects.genivi.org/ionas-hmi.git

The project is a standard Eclipse plug-in project extending the Eclipse
IDE. For building the project, you may either use the IDE's internal
build or the Maven/Tycho automatic build we provide.

The implementation of the Connector language and tools is an
Xtext-based domain-specific language (DSL). This DSL implementation and
the actual model transformation is not part of this project; if you
would like to participate in its development, you should refer to the
following AUTOSAR Subversion repository:

   https://svn.autosar.org/repos/work/25_Concepts/Incorporated/CP_R4.2/CONC_610_IntegrationOfNonARSystems/Test


-------------------------------------------------------------------------
Klaus Birken <klaus.birken@itemis.de>
Marco Eilers <marco.eilers@itemis.de>
