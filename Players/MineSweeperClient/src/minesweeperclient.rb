#!/usr/bin/env ruby
require_relative 'lib/client'
require_relative 'lib/interpreter'
require_relative 'lib/agent'
require_relative 'lib/mysocket'
require_relative 'lib/core_ext'
require_relative 'lib/mylogger'
require_relative 'lib/multi_io'
require_relative 'lib/play'
require_relative 'lib/optparse'
require_relative 'lib/rain'

obj = OptParse.config_object(ARGV)
client = Client.new(obj)
client.start

