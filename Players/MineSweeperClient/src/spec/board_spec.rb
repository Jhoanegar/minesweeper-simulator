require_relative './spec_helper'

describe Board do
  it 'should initialize correctly' do
    board = Board.new do |new_board|
      new_board.cells = [["C","C","C"],["C","C","C"],
               ["C","C","C"],["C","C","C"]]
      end
    
    board.width.should == 3
    board.height.should == 4
    board.cells.should have(4).items
    board.cells.each { |e| e.should have(3).items }
  end

  it 'should return the correct cell' do
    board = Board.new() {|b| b.cells = [["1","2"],["3","4"]] }
    board.cell(1,1).should == "4"
  end

  it 'should return nil if the cell is out of bounds' do
    board = Board.new() {|b| b.cells = [["1","2"],
                                        ["3","4"],
                                        ["5","6"]]}
    board.cell(2,1).should be nil
    board.cells.should have(3).items
  end

  it 'should yield all the cells' do
    board = Board.new() {|b| b.cells = [["1","2"],["3","4"]] }
    board.map {|cell| cell}.should == ["1","2","3","4"]

  end

  it 'should yield all the neighbours of a cell' do
    board = Board.new {|b| b.cells = [
                                     ["1","2","3"],
                                     ["4","5","6"],
                                     ["7","8","9"]] }
    results = []
    board.each_neighbour(1,1) do |cell,nx,ny|
      results << cell
    end
    results.should =~ [1,2,3,4,6,7,8,9]
  end
  
  it 'should only yield the neighbours within range' do
    board = Board.new {|b| b.cells = [
                                     ["1","2","3"],
                                     ["4","5","6"],
                                     ["7","8","9"]] }
   

    results = []
    board.each_neighbour(0,1) do |cell,nx,ny|
      results << cell
    end
    results.should =~ [1,2,5,7,8]
  end 

  it 'should parse and get any cell' do
    logger = double()
    logger.stub(:info)
    inter = Interpreter.new(logger)
    message = "BE 0 5 2 C C 
                        C C
                        C F
                        C C
                        E C"
    cells = inter.parse_board(message)[1][3]
    board = Board.new {|b| b.cells = cells}
    board.height.should == 5
    board.width.should == 2
    board.cell(1,2).should == "F"
    board.cell(0,4).should == "E"
  end
end    


