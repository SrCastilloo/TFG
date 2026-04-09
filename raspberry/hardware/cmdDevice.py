from .i2cdevice import I2cDevice
from time import sleep
import traceback
from configparser import ConfigParser
import RPi.GPIO as GPIO 


''' 
	Custom exceptions for this class
'''

class wrongDataFormatException(Exception):
	"The recieved raw data has not the correct format."
	pass

class unknownCommandException(Exception):
	"The command sent cannot be found."
	pass

class deviceFailureException(Exception):
	"The device has an error."
	pass

class noI2CAdress(Exception):
	"No I2C adress was found in the config file."
	pass


class CmdDevice:
	''' 
		Description: This class represents an command reciever who communicates via I2C
		Version: 1.2
		Author: Práxedes Neira, Kevin Vogel
	'''

	def __init__(self, name, conf_file) -> None:
		'''
			Init of the class CmdDevice
			conf_file: Should include the path for the config file
			name: Name of the device, this has to correspond with the subsection name of the config file
		'''
		self.__name = name

		self.__status = {"command": None, "detection_quality": None}
		self.__last_status = {"command": None, "detection_quality": None} 

		''' I2C communication information '''
		self.__nr_bytes_requested = None
		self.__i2c_offset = None
		self.__i2c_adr = None
		self.__max_attempts = None
		self.__accepted_commands = []
		self.__range_detection_quality = []
		self.__last_msg = None

		''' Load config file '''
		self.__config = ConfigParser()

		''' Init of i2c and load the config data'''
		try:
			self.__config.read(conf_file)           # read the config file
			self.__load_config()                    # load config data
			self.__i2c = I2cDevice(self.__i2c_adr)  # create I2C comunication object
		except KeyError as e:
			print(traceback.format_exc(), f"There has been a KeyError in the config file. Check the config file and the path.\nKey: {str(e)} does not exist.")
		except OSError as e:
			print(traceback.format_exc(),f"Remote I/O error occurred: {str(e)} \nCheck I2C connection and address.")
		except Exception as e:
			print(traceback.format_exc(), f"An error has occurred: {str(e)}")
		else:
			print(f"Command Device with name {self.__name} sucessfully initialised.")
		
		

	''' 
		Private methods of the class
	'''

	def __set_current_to_last_status(self):
		''' 
			sets the current status "self.__status" to the last status "self.__last_status"
		'''
		for key in self.__status.keys():
			self.__last_status[key] = self.__status[key]


	def __load_config(self):
		''' 
			Loads the necessary data and information from the config file into the class 
		'''

		''' Load i2c commands '''
		cmd = self.__config[self.__name]['uno'] # command is given in hex so we store it as string
		self.__accepted_commands.append(cmd)
		cmd = self.__config[self.__name]['dos']
		self.__accepted_commands.append(cmd)
		cmd = self.__config[self.__name]['tres']
		self.__accepted_commands.append(cmd)
		cmd = self.__config[self.__name]['cuatro']
		self.__accepted_commands.append(cmd)
		cmd = self.__config[self.__name]['cinco']
		self.__accepted_commands.append(cmd)
		cmd = self.__config[self.__name]['ruido']
		self.__accepted_commands.append(cmd)
		cmd = self.__config.getint(self.__name,'min_quality')
		self.__range_detection_quality.append(cmd)
		cmd = self.__config.getint(self.__name,'max_quality')
		self.__range_detection_quality.append(cmd)

		''' Load communication format/information'''
		self.__nr_bytes_requested = self.__config.getint(self.__name,'nr_bytes_receive')
		self.__i2c_offset = self.__config.getint(self.__name,'i2c_offset')
		self.__max_attempts = self.__config.getint(self.__name,'max_attempts')
		try:
			self.__i2c_adr = int(self.__config[self.__name]['i2c_adress'],16) # adress is given in hex so convert it
		except KeyError:
			raise noI2CAdress("No I2C adress was found in the config file. Please check the config file and the subsection name of the capacitive control device.")
		except ValueError:
			raise noI2CAdress("Invalid format for I2C address in the config file. Expected hexadecimal (e.g., 0x2b).")


	def __process_raw_data(self, raw_data):
		''' 
			Converts the raw_data and saves the information into self.__status 
		''' 
		if len(raw_data) != self.__nr_bytes_requested:
			raise wrongDataFormatException(f"The recieved raw data has a length of {len(raw_data)} but the expected length is {self.__nr_bytes_requested}")
		
		self.__last_msg = raw_data
		command = hex(raw_data[0])
		det_quality = raw_data[1]
		print(f"From <{self.get_name()}> processed command <{command}> binary<{bin(raw_data[0])}> type of command is {type(command)}, quality= <{det_quality}> type of quality is {type(det_quality)}")

		if command not in self.__accepted_commands:
				raise unknownCommandException(f"The command < {command} > sent from the device < {self.__name} > in the message < {raw_data} > is not in the list of accepted commands {self.__accepted_commands}")
				
		if det_quality > max(self.__range_detection_quality) or det_quality < min(self.__range_detection_quality):
			raise wrongDataFormatException(
				f"The detection quality has value of {det_quality}, but should be in range of {min(self.__range_detection_quality)} to {max(self.__range_detection_quality)} as defined in the config file for the device <{self.__name}>."
            )
		
		self.__set_current_to_last_status()          
		self.__status["command"] = command
		self.__status["detection_quality"] = det_quality




	''' 
		Public methods of the class
	'''
	def update(self):
		''' 
			Requests information from the command device via I2C and saves it into "self.__status" 
		'''
		attempt = 0
		last_error = None
		successfully_read = False
		while (attempt < self.__max_attempts) and not successfully_read:
			try:
				cmd_raw_data = self.__i2c.read_bytes(self.__i2c_offset, self.__nr_bytes_requested)
				print(f"Raw data from i2c: {cmd_raw_data}")
				self.__process_raw_data(cmd_raw_data)
				successfully_read = True
			except OSError as e:
				print(traceback.format_exc(),str(e))
				last_error = e
				attempt += 1
			except wrongDataFormatException as e:
				print(traceback.format_exc(),str(e))
				last_error = e
				attempt += 1
			except unknownCommandException as e: # sometimes commands not read properly it helps to request the data again
				print(f"Attempt Nr {attempt+1} to read from <{self.__name}> failed with the error: {str(e)}")
				last_error = e
				attempt += 1
			except Exception as e:
				print(traceback.format_exc(), str(e))
				last_error = e
				attempt += 1

		if not successfully_read:
			raise deviceFailureException(f"The device <{self.__name}> was {self.__max_attempts} times not callable. The last error of the device was: {str(last_error)}.")


	def print_status(self):
		''' 
			Prints the current status of the command device to the console
		'''
		print(f"The status of <{self.__name}> is {self.__status}")


	def print_config_data(self):
		''' 
			Prints the parameters and commands that were been loaded from the config file.
		'''
		print(f"The commands are: {self.__accepted_commands}")
		print(f"The range of detection quality is: {self.__range_detection_quality}")
		print(f"The I2C communication parameters are: i2c addr={self.__i2c_adr}; i2c offset={self.__i2c_offset}; nr bytes requested={self.__nr_bytes_requested}; max attempts={self.__max_attempts}")


	def get_status(self):
		''' 
			Returns the status of the command device 
		'''
		return self.__status


	def get_name(self):
		''' Returns the name of the device '''
		return self.__name


	def get_command(self):
		''' 
			Returns the command stored into status
		'''
		return self.__status["command"]


	def get_detection_quality(self):
		''' 
			Returns the detection quality (in %) stored into status
		'''
		return self.__status["detection_quality"]


	def new_data(self):
		'''
			Compares the dictionarys "self.__status" and "self.__last_status"
			Return "True" if the values are diffrent between "self.__status" and "self.__last_status" else return "False"
		'''
		if self.__status != self.__last_status:
			return True
		else:
			return False 
		
		
	def clear_status(self):
		''' 
			Clears the status and last status of the command device
		'''
		self.__status = {"command": None, "detection_quality": None}
		self.__last_status = {"command": None, "detection_quality": None}
		print(f"Status and last status of <{self.__name}> cleared.")
		
		
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
    Testing methods
'''

def test_speech_recognition():
	''' 
		test method for the speech recognition device
	'''
	name = "cmd_speech"
	conf_file = "config.ini"
	test = CmdDevice(name, conf_file)

	pin_wkup_speech = 10 # GPIO pin for wake up the voice microcontroller

	GPIO.cleanup() # Reset GPIO config if existing
	GPIO.setmode(GPIO.BCM) # set the used pin numbers to the Broadcom SOC channel numbers
	GPIO.setwarnings(False) # disable the warnings
	GPIO.setup(pin_wkup_speech, GPIO.OUT, initial=GPIO.LOW) # Init of the wake up pin

	while 1:
		sleep(1)
		GPIO.output(pin_wkup_speech, GPIO.HIGH) # wake up the voice microcontroller
		sleep(0.05) # wait a little bit to let the voice microcontroller wake up
		GPIO.output(pin_wkup_speech, GPIO.LOW) 
		sleep(5)
		test.update()
		test.print_config_data()
		test.print_status()
		print()


def test_new_cmd_device():
	'''
		Test method for testing the cmdDevice with the ASCII coding and the percentage of detection
	'''
	i2c_adr = 0x33
	name = "newDevice"
	test = CmdDevice(i2c_adr, name)

	while 1:
		sleep(0.5)
		test.update()
		test.print_status()
		print("\n")

''' 
	Testing area
'''
if __name__ == "__main__":
	test_speech_recognition()
	#test_new_cmd_device()