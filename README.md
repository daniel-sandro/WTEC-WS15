# Battleship

## Presentation

Link to the presentation [presentation](https://docs.google.com/presentation/d/1Ouwna6XV-nnGf2oqD8lEN9NRdYSNB6G4NPZAu45MpDg/edit?usp=sharing "Klick here to go to the presentation")


## Dependencies

This project depends on the *stateless-controller* branch of the *Battleship* project (https://github.com/daniel-sandro/Battleship/tree/stateless-controller). This has been added as a submodule under the *lib/* folder.

Therefore, the project has to be cloned recursively using the `--recursive` flag:

```
$ git clone --recursive https://github.com/daniel-sandro/WTEC1516.git
```

Or initialize the submodule after cloning the project:

```
$ git clone https://github.com/daniel-sandro/WTEC1516.git
$ git submodule init
$ git submodule update
```

## Development

To continue the development of this game you need to install node.js on your development environment.

To install all the dependencies open your terminal, change to the root of this project and type:

```
$ npm install
```

This installs all the necessary npm modules.

Then type:

```
$ npm install -g bower
```

This installs the bower plugin globally (-g) on your machine.

Finally, run

```
$ bower install
```

to download all the dependencies for this app.


To develop run `gulp`, which starts the watch task. The task watches the CSS files and compiles them if any changes were made.

## Deployment

The project can be deployed to Heroku using the Heroku SBT plugin (https://github.com/heroku/sbt-heroku).

To do so you have to change the following line in the file `build.sbt` to include your Heroku app name:

```
herokuAppName in Compile := "your-heroku-app-name"
```

Next, run the following command to deploy the app to Heroku:

```
$ sbt stage deployHeroku
```
