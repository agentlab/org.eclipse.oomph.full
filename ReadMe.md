
Eclipse Oomph fork with enabled source features generation

= How to import into Eclipse IDE =
* Clone repository
* Run Eclipse Installer (Oomph)
* Switch Eclipse Installer to Agentlab Products Catalog
* Select Java IDE with Maven and Tools product
* Drag and drop Oomph2.setup into Projects tree in Eclipse Installer
* Select newly appeared Oomph prject
* Continue and install new Eclipse IDE installation

= How to rebuild =
mvn clean package -Pgenerate-source
Build will fail without generate-source profile

Update site will be in sites/org.eclipse.oomph.site/target

= How to run =
Run launch config in Eclipse IDE
