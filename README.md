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

## Algorithm

The producer divides stream of digits into chunks for distributed processing (with proper overlapping to make sure it won't miss any result). Then it sends out work to consumers.

The consumer receives a chunk to search as well as a reference score which should be improved. If it finds a match scoring more than this reference, it sends the result back to sender.

The score is computed based on pattern of white, gray and black pixels, represented by ranges of colors. The reason for that is that human eye does not really see that much of a difference between `#ffffff` and `#ebebeb` or `#333333` and `#000000` for example, on a small image. If a pixel matches its range defined in pattern, it is worth some points. White is worth 5 points because the negative space around the logo really helps to draw its shape. Other ranges are worth 1 point. The sum of points of all 84 pixels constitutes result score.

To improve performance, we don't need to recalculate points for every of the pixels every time. We can use the scores from previously computed match and calculate only the scores at breakpoints - the points where pattern changes. In addition, we know the maximum possible score difference between consecutive windows - if current score is much smaller than reference maximum, we can skip appropriate number of next matches, because they won't improve the result.

## Performance

Full search of billion digits takes 760s on my 4-core Macbook Air. It could be faster if you have more cores, run it on a cluster or tweak akka settings.
