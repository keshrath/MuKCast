/**
 * This code is copyright (c) Mathias Markl 2016
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */


import at.mukprojects.mukcast.client.*;
import at.mukprojects.mukcast.message.*;

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