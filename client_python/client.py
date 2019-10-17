import socket

import labmonitor_pb2 as lab


def chatWithServer(msg):

    HOST = '127.0.0.1'
    PORT = 7896
        
    tcp = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    dest = (HOST, PORT)
    tcp.connect(dest)


    tcp.send(struct.pack("H", len(msg)))    #send a two-byte size field
    tcp.send(msg)

    response = lab.ServerResponse()
    print(tcp.recv(1024))
    data = response.ParseFromString(tcp.recv(1024))
    print("received from server: " + data)
    tcp.close()


def main():
    opt = raw_input("Sensor or Actuator? ")  # "sensors"

    if (opt.lower() == "sensor"):

        client = lab.ClientRequest()
        client.reqType = lab.ClientRequest.ClientRequestType.GET_SENSORS_DATA
        chatWithServer(client.SerializeToString())

    elif (opt.lower() == "actuator"):

        actuator = lab.Actuators()
        opt = raw_input("Enter the actuator option: led or buzzer")  # "led"

        if(opt.lower() == "led"):
            print("*** LED ***")
            # "green"  # or red or yellow
            color = raw_input("Enter the led color: green, red or yellow")
            if (cor != "red" and cor != "green" and cor != "yellow"):
                print("!!! Invalid color !!!")
                return
            status = raw_input("Enter the led status: on or off")  # "on"
            if (status != "on" and status != "off"):
                print("!!! Invalid status !!!")
                return

            led = actuator.Led.add()
            led.color = color
            led.status = status

        elif (opt.lower() == "buzzer"):

            print("*** BUZZER ***")

            freq = int(raw_input("Enter the buzzer frequency (ms):"))  # 500
            if (freq <= 0):
                print("!!! Invalid frequency !!!")
                return

            duration = int(raw_input("Enter the buzzer duration (ms):"))  # 100
            if (duration <= 0):
                print("!!! Invalid duration !!!")
                return

            times = int(raw_input("Quantas repeticoes:"))  # 3
            if (times <= 0):
                print("!!! Invalid repetition number !!!")
                return

            status = raw_input("Enter the buzzer status: on or off")  # "on"
            if (status == ""):
                print("!!! Invalid status  !!!")
                return

            buzz = actuator.Buzzer.add()

            buzz.freq = freq
            buzz.duration = duration
            buzz.times = times
            buzz.status = status
        else:
            print("!!! Invalid option !!!")
            return
        chatWithServer(actuator.SerializeToString())
    else:
        print("!!! Invalid option !!!")


if __name__ == '__main__':
    print("Yeh")
    main()

print("Closing...")
