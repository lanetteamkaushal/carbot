## How to install CAN 232 adapter into Mini Cooper ##

Remove 3 screws at the  bottom of the driver's foot well cover.
<table><tr><td><a href='http://picasaweb.google.com/lh/photo/qdR63cY1DFPj1474PAzG2Q?feat=embedwebsite'><img src='http://lh4.ggpht.com/_pKFSZGJTV1g/TD1SmqSs-4I/AAAAAAAABYc/GD9aPF6ddHo/s800/footWellBolts.JPG' /></a></td></tr></table>

Now pull the top of the foot well cover towards you. It might feel like you're going to break it. You won't!

You need to get to this 18-pin connector. You should be able to reach it with a hand an pull it to detach it from the steering column.
<table><tr><td><a href='http://picasaweb.google.com/lh/photo/K5hZHfPejhbe3908Sn9Ulw?feat=embedwebsite'><img src='http://lh6.ggpht.com/_pKFSZGJTV1g/TD1SmqFO7QI/AAAAAAAABYg/wVB50aUD2E4/s800/footWellConnector.JPG' /></a></td></tr></table>

This is how it looks like when you detach it
<table><tr><td><a href='http://picasaweb.google.com/lh/photo/Pi_ZWk1cJluUeTQ_IgAu7Q?feat=embedwebsite'><img src='http://lh4.ggpht.com/_pKFSZGJTV1g/TD1Sm673csI/AAAAAAAABYk/xvVteQSpEAs/s800/footWellConnector2.JPG' /></a></td></tr></table>

You might want to detach tachometer to get easier access to the connector. There are 2 bolts that hold tachometer, so it is super-easy to dismount it.

Connect CAN232 to wires of 18-pin connector as follows
|GREEN | K\_CAN\_H |
|:-----|:----------|
|ORANGE/GREEN | K\_CAN\_L |
|YELLOW/RED | POWER     |
|BROWN | GROUND    |

I've soldered a wire within CAN232 that goes from POWER on CAN side to pin-9 on RS232 side, this way I power my Bluetooth serial adapter off CAN232. No wires needed :-)