require_relative './spec_helper'

describe Interpreter do
  before(:each) do
    logger = double()
    logger.stub(:info)
    @inter = Interpreter.new(logger)
  end
  
 
  it 'should give you the name of the player' do
    message = "(REG OK P1)"
    @inter.decode(message)
    @inter.response.should == "P1"
  end

  it 'should say when the game starts' do
    message = "(GE 0 ON 10)"
    @inter.decode(message)
    @inter.response[0].should == "ON"
  end

  it 'should give you the score as an integer' do
    message = "(GE #{Random.rand(0..1000)} SCORE 4)"
    @inter.decode(message)
    @inter.response.should == ["SCORE",4]
  end

  it 'should give the final statistics as strings' do
    message = "(GE 3 FIN P1 10 2 2)"
    @inter.decode(message)
    @inter.response.should == ["FIN",["P1","10","2","2"]]
  end
 
  it 'should parse correctly the board' do
    message = "(BE 10 4 3 00 01 02 10 11 12 20 21 22 30 31 32)" 
    @inter.decode(message)
    @inter.response.should ==
      ["BE",[10, 4, 3,
        [["00","01","02"],
         ["10","11","12"],
         ["20","21","22"],
         ["30","31","32"]]]]
  end
end


