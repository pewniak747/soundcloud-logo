#!/usr/bin/env ruby

require 'imageruby'

regex = /Score: (\d+); Sequence: (\d+); Offset: (\d+)/
width = 14
height = 6
colors = [
  ImageRuby::Color.white,
  ImageRuby::Color.coerce("#f0f0f0"),
  ImageRuby::Color.coerce("#ebebeb"),
  ImageRuby::Color.coerce("#d0d0d0"),
  ImageRuby::Color.coerce("#c1c1c1"),
  ImageRuby::Color.coerce("#a8a8a8"),
  ImageRuby::Color.coerce("#878787"),
  ImageRuby::Color.coerce("#535353"),
  ImageRuby::Color.coerce("#333333"),
  ImageRuby::Color.black
]

while line = readline do
  if line =~ regex
    match = line.match(regex)
    score = match[1]
    sequence = match[2]
    offset = match[3]

    image = ImageRuby::Image.new(width, height, ImageRuby::Color.white)
    sequence.split(//).map(&:to_i).each_with_index do |num, idx|
      image[idx % width, idx / width] = colors[num]
    end

    filename = "#{score}-#{sequence}-#{offset}.bmp"
    full_filename = File.join("results", filename)
    image.save(full_filename, :bmp)
    puts "Saved #{filename}"
  end
end
