require 'logger'
require 'date'
# Allows the construction of a customized Logger object that outputs an event
# log in both STDOUT and a text file.
module MyLogger
  # Creates a new Logger object with a custom format for its output.
  # @param file_name [String] the name of the file to create with the log.
  # @level [Fixnum] the minumun level of severity that the logger will output.
  # @return [Logger] the customized logger object
  def MyLogger.new_logger(file_name, verbose)
    if verbose == true
      level = Logger::DEBUG
    else
      level = Logger::INFO
    end

    file = File.new(file_name,"w")

    log = Logger.new(MultiIO.new(STDOUT, file))
    log.level = level
    log.formatter = proc do |severity, datetime, prog_name, msg|
      date = datetime.strftime("%T,%L")
      "[#{date}] #{severity} -- :\n  #{msg}\n"
    end
    log.info "Logger created"
    return log
  end
end
