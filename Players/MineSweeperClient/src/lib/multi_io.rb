# Defines a custom IO device that, instead of just having one output
#  object, has several. This allows the Logger class to log to several streams.
class MultiIO
	# Initializes the object, storing in an array the objects for future log.
	# @param [#write,#close] the objects to store the log
  def initialize(*targets)
    @targets = targets
  end

  # Called by Logger, writes the message in each of the @targets.
  def write(*args)
    @targets.each {|t| t.write(*args)}
  end

  # Called by Logger, closes each output stream.
  def close
    @targets.each(&:close)
  end
end
