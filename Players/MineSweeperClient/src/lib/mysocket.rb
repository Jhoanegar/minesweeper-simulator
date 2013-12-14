require 'socket'
# Handles the communication between the program and the server.
class MySocket < UDPSocket
  # Initializes the UDP socket.
  # @param logger [Logger] an object to log the events.
  # @config [#server,#host] contains the addresses of both the client
  #   and the server.
  def initialize(logger, config)
    @log = logger
    @server_address = config.server.address
    @server_port = config.server.port

    @log.info "Socket: Binded to #{config.host.address}:#{config.host.port}"
    @log.info "Socket: Server at #{@server_address}:#{@server_port}"

    super(Socket::AF_INET)
    begin
      bind(config.host.address,config.host.port)
    rescue Errno::EADDRINUSE
      @log.error "Address already in use, please use a different address."
      abort
    end
  end

  # Waits until the server sends a package to the client and returns
  #  only the data, discarding the AF_INET package details.
  # return <String> the data received.
  def listen
    begin
      re = recvfrom(65536)
    rescue Errno::ECONNRESET
      @log.error "Couldn't reach the server. Is the server running?"
      abort
    end
    @log.info "Received #{re[0]} from #{re[1][2]}:#{re[1][1]}"
    return re[0]
  end

  # Sends a message to the server.
  # @param message [String] the message in the correct syntax.
  def send(message)
    super message, 0, @server_address, @server_port
    @log.info "Sent #{message} to #{@server_address}:#{@server_port}"
  end
end
