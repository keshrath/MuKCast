# MuKCast

MuKCast is an light-weight client/server library for the [Processing](http://processing.org/) Development Environment (PDE).

MuKCast can be used to send messages between a client and a server. There are three predefined messages in the library.

* TextMessage
* NumberMessage
* ImageMessage

In case there is a need for a customized message, it is possible to create a custom message by implementing the interface `Message.java`.

## Example Server

```java
MuKCastServer server;

PImage img1, img2;

void setup() {
  size(800, 450);
  frameRate(5);

  textAlign(CENTER, CENTER);

  img1 = loadImage("vstreamsphere01.jpg");
  img2 = loadImage("vstreamsphere02.jpg");

  img1.resize(800, 450);
  img2.resize(800, 450);

  server = new MuKCastServer(this);
  try {
  	server.startServer();
  } 
  catch(IOException e) {
    e.printStackTrace();
  }

  image(img1, 0, 110, width/2, 225);
  image(img2, width/2, 110, width/2, 225);

  textSize(48);
  text("Server: " + server.getServerInfo(), width/2, height/2);
}

void draw() {

  surface.setTitle("Server - FPS: " + frameRate);

  try {
    if (frameCount % 2 == 0) {
      server.broadcastMessage(new ImageMessage(img1));
    } else {
      server.broadcastMessage(new ImageMessage(img2));
    }
  } 
  catch(IOException e) {
    e.printStackTrace();
  }
}
```

## Example Client

```java
MuKCastClient client;

PImage image;

void setup() {
  size(800, 450);

  textAlign(CENTER, CENTER);

  client = new MuKCastClient(this, "localhost", 4242);
  image = createImage(800, 450, RGB);
  
  try {
    client.connect();
  } 
  catch(IOException e) {
    e.printStackTrace();
  }
}

void draw() {
  background(0);

  surface.setTitle("Client - FPS: " + frameRate);

  if (image != null) {
    image(image, 0, 0, width, height);
  }

  textSize(48);
  text("Client: " + client.getClientInfo(), width/2, height/2);
}

void handleMessage(MuKCastClient client, Message message) {
  image = ((ImageMessage) message).getImage(image);
}
```

## How to install

Download MuKCast library from [here](https://github.com/keshrath/MuKCast/blob/master/distribution/MuKCast/download/MuKCast.zip).

Unzip and copy it into the `libraries` folder in the Processing sketchbook. You will need to create this `libraries` folder if it does not exist.

To find (and change) the Processing sketchbook location on your computer, open the Preferences window from the Processing application (PDE) and look for the "Sketchbook location" item at the top.

By default the following locations are used for your sketchbook folder: 
  * For Mac users, the sketchbook folder is located inside `~/Documents/Processing` 
  * For Windows users, the sketchbook folder is located inside `My Documents/Processing`

The folder structure for library Console should be as follows:

```
Processing
  libraries
    MuKCast
      examples
      library
      reference
      src
      library.properties
```