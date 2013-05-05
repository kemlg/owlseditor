# owlseditor

This project aims to create an easy-to-use editor for creating OWL-S services. The editor is being developed as a plugin to the Protégé Ontology Editor.

Originally, the project was maintained at [SemWebCentral](http://projects.semwebcentral.org/projects/owlseditor/) ([old web page](http://owlseditor.semwebcentral.org/index.shtml)). However, there has been some time without updates and the plugin was not working anymore for Protégé version 3.4+.

This GitHub project aims to build upon work already done and provide a plugin that works with the current releases of Protégé, maintain it, and take advantage of the GitHub community in case people out there want to contribute.

This repository is being maintained by the [KEMLg](http://kemlg.upc.edu) research group, and the original developer of the plugin before the migration to GitHub is [SRI International](http://www.sri.com).

## Binary release

* Version 1.2.2 [[zip](http://dl.bintray.com/content/kemlg/owlsutils/com/sri/owlseditor/1.2.2/owlseditor-1.2.2.zip?direct)] (tested in Protégé 3.4.8)

## Installation

In order to install, unzip the binary release into `${protege.dir}/plugins/`.

## Building the plug-in

In order to build owlseditor, you will need to have Maven installed. Clone this repository and run:

    mvn clean package

The plug-in will be packed into a zipfile inside the target/ folder.
