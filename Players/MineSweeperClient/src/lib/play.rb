# Represents an agent's play.
class Play

  # @!attribute [rw] command
  #   @return [String] the string representation of the
  #    command in the format understood by the server.
  # @!attribute [rw] coords
  #   @return [Struct] an object that responds to .x and .y
  #    representing the coordinates of the play.
  # @!attribute [rw] x
  #   @return [Fixnum] the column of the play.
  # @!attribute [rw] y
  #   @return [Fixnum] the row of the play.
  
  attr_accessor :command

  # Initializes the play.
  # @param x [#to_i] the number of the column.
  # @param y [#to_i] the number of the row.
  # @param cmd [String] the command of the play.
  # @yieldparam [Play] returns self.
  def initialize(x=nil,y=nil,cmd=nil)
    @coords = Coords.new(x.to_i,y.to_i)
    @command = cmd
    yield self if block_given?
  end

  # Returns the complete command of the play.
  # Return [String] a string representation of the play.
  def to_command
    "(#{command} #{@coords.x} #{@coords.y})"
  end
  
  # Two plays are equal iff their respective coordinates
  #  and commands are equal.
  # @param other [Play] the play to be comapared to self.
  def == other
    if @coords == other.coords and @command == other.command
      return true 
    else
      return false
    end
  end

  def x=(new_x)
    @coords.x = new_x.to_i
  end

  def y=(new_x)
    @coords.y = new_x.to_i
  end

  def x
    @coords.x
  end

  def y
    @coords.y
  end

  def coords
    @coords
  end
  # Simple class to group the coordinates of a play in
  #  a single variable.
  class Coords < Struct.new(:x,:y)
  end
end


