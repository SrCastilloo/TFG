from .i2cdevice import I2cDevice
from time import sleep
import traceback
from configparser import ConfigParser

''' 
	Custom exceptions for this class
'''
class wrongDataFormatException(Exception):
	"The recieved raw data has not the correct format."
	pass

class deviceFailureException(Exception):
	"The device has an error."
	pass

class noI2CAdress(Exception):
	"No I2C adress was found in the config file."
	pass

class unknownNumberOfSensorsException(Exception):
	"The Number of capacitive sensors to adress is unknown."
	pass


class CapacitiveControl():
	''' 
		Description: This class represents the capacitive microcontroller on the PCB.
		Version: 2.0
		Author: Práxedes Neira
	'''

	''' Constants of the class '''

	__nr_bytes_per_sensor = None
	__nr_used_sensors = None
	__nr_bytes_requested = None
	__sensor_strings = None

	__sensor_heights = {"pinky": None, "ring" : None, "middle" : None, "index" : None, "thumb" : None, "palm" : None}



	def __init__(self, name, conf_file) -> None:
		'''
			Init of the class CapacitiveControl
			conf_file: Should include the path for the config file
			name: Name of the device, this has to correspond with the subsection name of the config file --> cc_hand
		'''
		self.__name = name

		''' Difference in the capacitance of each sensor '''
		self.__status = {"pinky": None, "ring": None, "middle": None, "index": None, "thumb": None, "palm": None}

		''' I2C communication information '''
		self.__i2c_offset = None
		self.__i2c_adr = None

		''' Load config file '''
		self.__config = ConfigParser()

		''' Init of i2c, load the config data and read status of the hand'''
		try:
			self.__config.read(conf_file)           # read the config file
			self.__load_config()                    # load config data
			self.__i2c = I2cDevice(self.__i2c_adr)  # create I2C comunication object
			self.update_status()
		except KeyError as e:
			print(traceback.format_exc(), f"There has been a KeyError in the config file. Check the config file and the path.\nKey: {str(e)} does not exist.")
		except OSError as e:
			print(traceback.format_exc(),f"Remote I/O error occurred: {str(e)} \nCheck I2C connection and address.")
		except Exception as e:
			print(traceback.format_exc(), f"An error has occurred: {str(e)}")
		else:
			print("CapacitiveControl sucessfully initialised.")
		


	''' 
		Private methods of the class
	'''

	def __load_config(self):
		''' 
			This method should load the config data from a config file 
		'''

		''' Load communication format/information'''
		self.__nr_bytes_requested = self.__config.getint(self.__name,'nr_bytes_receive')
		self.__nr_bytes_per_sensor = self.__config.getint(self.__name,'nr_bytes_per_sensor')
		self.__nr_used_sensors = self.__config.getint(self.__name,'nr_used_sensors')
		if self.__nr_used_sensors == 6:
			self.__sensor_strings = ["pinky", "ring", "middle", "index", "thumb", "palm"]
		else:
			raise unknownNumberOfSensorsException
		self.__i2c_offset = self.__config.getint(self.__name,'i2c_offset')
		try:
			self.__i2c_adr = int(self.__config[self.__name]['i2c_adress'],16) # adress is given in hex so convert it
		except KeyError:
			raise noI2CAdress("No I2C adress was found in the config file. Please check the config file and the subsection name of the capacitive control device.")
		except ValueError:
			raise noI2CAdress("Invalid format for I2C address in the config file. Expected hexadecimal (e.g., 0x2b).")

		''' Load height values for each sensor '''
		self.__sensor_heights['pinky'] = self.__config.getint('cc_heights','pinky_height')
		self.__sensor_heights['ring'] = self.__config.getint('cc_heights','ring_height')
		self.__sensor_heights['middle'] = self.__config.getint('cc_heights','middle_height')
		self.__sensor_heights['index'] = self.__config.getint('cc_heights','index_height')
		self.__sensor_heights['thumb'] = self.__config.getint('cc_heights','thumb_height')
		self.__sensor_heights['palm'] = self.__config.getint('cc_heights','palm_height')


	def __process_raw_data(self, raw_data):
		''' 
			Parses the raw data recieved via I2c from the capacitive microcontroller into the dictionary "self.__status"
			This method is written for 6 sensors so in total 12 bytes, for other formats the code needs to be changed.
		'''
		len_raw_data = len(raw_data)
		if len_raw_data != self.__nr_bytes_requested:
			raise wrongDataFormatException(f"The recieved raw data from the capacitive microcontroller has the wrong length.\nThe expectetd number of bytes is {self.__nr_bytes_requested} but recieved where {len_raw_data}")
		self.__status["pinky"] = raw_data[0] << 8| raw_data[1]
		self.__status["ring"] = raw_data[2] << 8| raw_data[3] 
		self.__status["middle"] = raw_data[4] << 8| raw_data[5]
		self.__status["index"] = raw_data[6] << 8| raw_data[7] 
		self.__status["thumb"] = raw_data[8] << 8| raw_data[9]
		self.__status["palm"] = raw_data[10] << 8| raw_data[11]


	''' 
		Public methods of the class
	'''
	def update_status(self):
		''' 
			Requests information from the auxilar microcontroller via I2C and saves it into "self.__status"
		'''
		try:
			raw_data = self.__i2c.read_bytes(self.__i2c_offset, self.__nr_bytes_requested)
			self.__process_raw_data(raw_data)
		except OSError as e:
			print(traceback.format_exc(),str(e))
		except Exception as e:
			print(traceback.format_exc(), str(e))



	def print_status(self):
		''' 
			Prints the current status of the command device to the console
		'''
		print(f"The status of <{self.__name}> is {self.__status}")


	def print_config_data(self):
		''' 
			Prints the parameters that were been loaded from the config file.
		'''
		print(f"The sensor heights are: {self.__sensor_heights}")
		print(f"The I2C communication parameters are: i2c addr={self.__i2c_adr}; i2c offset={self.__i2c_offset}; nr bytes per sensor={self.__nr_bytes_per_sensor}; nr used sensors={self.__nr_used_sensors}; nr bytes requested={self.__nr_bytes_requested}")


	def get_status(self):
		''' 
			Returns the status of the command device 
		'''
		return self.__status
	
	
	def get_heights(self):
		''' 
			Returns the sensor heights of the capacitive sensors
		'''
		return self.__sensor_heights


	def get_name(self):
		''' Returns the name of the device '''
		return self.__name


	def close_i2c(self):
		''' 
			Closes the I2C communication
		'''
		if self.__i2c is not None:
			self.__i2c.close()
			print(f"I2C communication with {self.__name} closed.")
		else:
			print("No I2C communication to close.")



''' 
	Testing area
'''
if __name__ == "__main__":
	conf_file = "config.ini"
	name = "cc_hand"
	test = CapacitiveControl(name, conf_file)
	test.print_config_data()
	sleep(3) # Wait for the microcontroller to start

	while True:
		test.update_status()
		test.print_status()
		sleep(2)
    