require 'set'
# Represents the board of the game
class Board
  # For each, map, and all the enumerable methods.
  include Enumerable
  # @!attribute [rw] cells
  #   @return [Array<Array>] a bidimensional Array containing the cells.
  # @!attribute [rw] width
  #   @return [Fixnum] the number of columns in the board.
  # @!attribute [rw] height
  #   @return [Fixmun] the number of rows in the board.
  
  
  attr_accessor :cells,:width, :height

  # Creates the board
  # @yieldparam [Board] returns self for explicit initialization.
  def initialize(cells = nil)
    @cells = nil
    yield self if block_given?
  end

  # Setter method for @cells.
  # @param new_cells [Array<Array>] the new cells of the board.
  def cells=(new_cells)
    @cells = new_cells
    @height = new_cells.size
    @width= new_cells[0].size
  end

  # Represents the board as a String.
  # @return [String] the board represented as a String.
  def to_s
    ret = "\n "
    width.times {|t| ret << (t % 10).to_s}
    ret << "\n"
    height.times do |i|
      ret << (i % 10).to_s
      width.times do |j|
        ret << case cell(j,i)
               when /C/
                 "\u2588".encode("utf-8")
               when /F/
                 "*"
               when /E/
                 "-"
               else
                 cell(j,i)
               end
               
      end
      ret << "\n"
    end
    ret
  end

  # Gets a specific cell in the board.
  # @param args [Array] if only one argument is received, it should respond to
  #  :x and :y methods that contain the coordinates of the cell. Else if, 
  #  two parameters are received, they would be treated as the x and y coordinate
  #  respectively.
  # @return [String] only the last character of the cell representing its state.
  def cell(*args)
    if args[0].respond_to?(:x) and args[0].respond_to?(:y)
      x = args[0].x
      y = args[0].y
    elsif args.size == 2
      x = args[0]
      y = args[1]
    end
    unless x >= self.width or y >= self.height or
        x < 0 or y < 0
      return @cells[y][x][-1]
    end
    return nil
  end

  # Iterates over the cells.
  # @yield [cell_state,x,y] gives the contentn and coordinates of the current cell.
  def each
    height.times do |y|
      width.times do |x|
        yield cell(x,y)[-1], x, y
      end
    end
  end
 
  # Iterates over the 8-conn neighbours of a cell
  # @param args [Array] if only one argument is received, it should respond to
  #  :x and :y methods that contain the coordinates of the cell. Else if, 
  #  two parameters are received, they would be treated as the x and y coordinate
  #  respectively.
  # @yield [cell_state,x,y] gives the contentn and coordinates of the current cell.
  def each_neighbour(*args)
    connections = [-1,0,1]
    if args[0].respond_to?(:x) and args[0].respond_to?(:y)
      x_coord = args[0].x
      y_coord = args[0].y
    elsif args.size == 2
      x_coord = args[0]
      y_coord = args[1]
    end
    connections.each do |x|
      connections.each do |y|
        nx = x_coord + x
        ny = y_coord + y
        nc = cell(nx,ny)
        unless nc.nil? or (x == 0  and y == 0)
          if nc[-1] =~ /\d/
            yield nc.to_i, nx, ny
          else
            yield nc, nx, ny
          end
        end
      end
    end
  end
end
