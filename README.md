# soundcloud logo in π

This program attempts to find soundcloud logo in decimal points of π. See full details here: https://developers.soundcloud.com/blog/buzzwords-contest.html

## Setup

You'll need scala with `sbt` to run this. Ruby with `bundle` is required for optional visualization.

```
sbt compile
bundle install
```

Also, download the file containing first billion digits of pi:

```
wget http://stuff.mit.edu/afs/sipb/contrib/pi/pi-billion.txt
```

## Running

Running the program will generate result `bmp`'s in `results/` directory.

```
mkdir results
sbt run < pi-billion.txt | bundle exec ruby image.rb
```

You can also run it without generating image results:

```
sbt run < pi-billion.txt
```

Or for first N digits:

```
head -c N pi-billion.txt | sbt run
```

## Results

Reference: ![](https://developers.soundcloud.com/assets/ref-df480d00485df7b8c1e47762d4d11430.gif)

| Rank | Image                                                                                        | Sequence                                                                             | Offset    |
|------|----------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------|-----------|
| 1    | ![](https://github.com/pewniak747/soundcloud-logo/blob/master/results-final/1.bmp?raw=true)  | 002018389971220218045582410106189055335572418584786986482348181759008404675658168791 | 986358010 |
| 2    | ![](https://github.com/pewniak747/soundcloud-logo/blob/master/results-final/2.bmp?raw=true)  | 800215341351220103034671310222446476806212424333014653707055343830806304853974200132 | 380812540 |
| 3    | ![](https://github.com/pewniak747/soundcloud-logo/blob/master/results-final/3.bmp?raw=true)  | 013117783721171024573721821112998426218872817454685486857689969988482702703608582971 | 891207526 |
| 4    | ![](https://github.com/pewniak747/soundcloud-logo/blob/master/results-final/4.bmp?raw=true)  | 280307573702001026282082852100079309773702383444978475064387355071369219962589860871 | 894673827 |
| 5    | ![](https://github.com/pewniak747/soundcloud-logo/blob/master/results-final/5.bmp?raw=true)  | 101629546982060207887930301111977156147512363476354872593937840472193120450665403961 | 16276211  |
| 6    | ![](https://github.com/pewniak747/soundcloud-logo/blob/master/results-final/6.bmp?raw=true)  | 011025068461120225365774490241062847066922484590533101154650966505636125139938896540 | 236678313 |
| 7    | ![](https://github.com/pewniak747/soundcloud-logo/blob/master/results-final/7.bmp?raw=true)  | 121008386632828006353692404001989056001112432233104896875966576194676313998734249682 | 475652356 |
| 8    | ![](https://github.com/pewniak747/soundcloud-logo/blob/master/results-final/8.bmp?raw=true)  | 122101648600091283386856422112074761947801134540905302315407968769948919384772415950 | 545665906 |
| 9    | ![](https://github.com/pewniak747/soundcloud-logo/blob/master/results-final/9.bmp?raw=true)  | 241114290882013014932027820220115719980001855927285687118987365526304416427417096760 | 810069638 |
| 10   | ![](https://github.com/pewniak747/soundcloud-logo/blob/master/results-final/10.bmp?raw=true) | 101012742930222023384572920712972751102542545231074489099239267715789219963276889672 | 877772334 |

## Algorithm

The producer divides stream of digits into chunks for distributed processing (with proper overlapping to make sure it won't miss any result). Then it sends out work to consumers.

The consumer receives a chunk to search as well as a reference score which should be improved. If it finds a match scoring more than this reference, it sends the result back to sender.

The score is computed based on pattern of white, gray and black pixels, represented by ranges of colors. The reason for that is that human eye does not really see that much of a difference between `#ffffff` and `#ebebeb` or `#333333` and `#000000` for example, on a small image. If a pixel matches its range defined in pattern, it is worth some points. White is worth 5 points because the negative space around the logo really helps to draw its shape. Other ranges are worth 1 point. The sum of points of all 84 pixels constitutes result score.

To improve performance, we don't need to recalculate points for every of the pixels every time. We can use the scores from previously computed match and calculate only the scores at breakpoints - the points where pattern changes. In addition, we know the maximum possible score difference between consecutive windows - if current score is much smaller than reference maximum, we can skip appropriate number of next matches, because they won't improve the result.

## Performance

Full search of billion digits takes 760s on my 4-core Macbook Air. It could be faster if you have more cores, run it on a cluster or tweak akka settings.
