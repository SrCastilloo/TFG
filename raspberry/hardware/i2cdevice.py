import smbus2
from smbus2 import i2c_msg
from time import sleep


class I2cDevice:
	''' class I2cDevice
		Decription: This class provides the communication via I2C Bus
		Version:    1.0
		Author:     Kevin Vogel
	'''
	offset = 0
	def __init__(self, address, bus=1):
		self.__address = address
		self.__bus = smbus2.SMBus(bus)



	def write_bytes(self, cmd, data):
		'''
			write_bytes(self, cmd, data): Writes via I2C to the adress "self__adress" the data provided in "data"
		'''
		msg = i2c_msg.write(self.__address,data)
		self.__bus.i2c_rdwr(msg)



	def read_bytes(self, cmd, num_bytes):
		'''
			read_bytes(self, cmd, num_bytes): Reads via I2C from the adress "self__adress" amount of bytes "num_bytes" and returns it in a list of bytes
		'''
		msg = i2c_msg.read(self.__address,num_bytes)
		try:
			self.__bus.i2c_rdwr(msg)
		except OSError as e:
			sleep(0.02) # TODO just for testing, the speech MC needs to be reseted every time you want to make a new detection, the delay time prevents the code from crashing
			self.__bus.i2c_rdwr(msg)

		data = list(msg)
		return data



	def close(self):
		'''
			close(self): Closes the I2C bus
		'''
		if self.__bus is not None:
			self.__bus.close()