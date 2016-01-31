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

import at.mukprojects.mukcast.server.*;
import at.mukprojects.mukcast.message.*;

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