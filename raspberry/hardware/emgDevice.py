from .i2cdevice import I2cDevice
from time import sleep
import traceback
from configparser import ConfigParser

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


class EmgDevice():
	''' 
		Description: This class represents an command reciever-sender for the emg detection microcontroller, which communicates via I2C
		Version: 2.0
		Author: Práxedes Neira, Kevin Vogel
	'''

	def __init__(self, name, conf_file) -> None:
		'''
			Init of the class EmgDevice
			conf_file: Should include the path for the config file
			name: Name of the device, this has to correspond with the subsection name of the config file
		'''
		self.__name = name

		self.__status = {"command": None, "detection_quality": None}
		self.__last_status = {"command": None, "detection_quality": None}
		self.__vibration_motors_strings = ["motor6", "motor7", "motor1", "motor2"]
		self.__target = {"start_emg_recognition": False, "motor6": False, "motor7": False, "motor1": False, "motor2": False}

		''' I2C communication information '''
		self.__nr_bytes_requested = None 
		self.__nr_bytes_sent = None
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
			print(f"Emg Device with name {self.__name} sucessfully initialised.")
	


	''' 
		Private methods of the class
	'''

	def __load_config(self):
		''' 
			This method should load the config data from a config file 
		'''
		
		''' Load array of accepted commands '''
		raw_value = self.__config[self.__name]['accepted_commands']
		self.__accepted_commands = [int(item.strip()) for item in raw_value.split(',')] # accepted_commands = [0, 1, 2, 3] int, not string
		
		''' Load range of acceptable detection quality '''
		cmd = self.__config.getint(self.__name,'min_quality')
		self.__range_detection_quality.append(cmd)
		cmd = self.__config.getint(self.__name,'max_quality')
		self.__range_detection_quality.append(cmd)

		''' Load communication format/information'''
		self.__nr_bytes_requested = self.__config.getint(self.__name,'nr_bytes_send_receive')
		self.__nr_bytes_sent = self.__config.getint(self.__name,'nr_bytes_send_receive')
		self.__i2c_offset = self.__config.getint(self.__name,'i2c_offset')
		self.__max_attempts = self.__config.getint(self.__name,'max_attempts')
		try:
			self.__i2c_adr = int(self.__config[self.__name]['i2c_adress'],16) # adress is given in hex so convert it
		except KeyError:
			raise noI2CAdress("No I2C adress was found in the config file. Please check the config file and the subsection name of the capacitive control device.")
		except ValueError:
			raise noI2CAdress("Invalid format for I2C address in the config file. Expected hexadecimal (e.g., 0x2b).")



	def __set_current_to_last_status(self):
		''' 
			Sets the current status "self.__status" to the last status "self.__last_status"
		'''
		for key in self.__status.keys():
			self.__last_status[key] = self.__status[key]


	def __process_raw_data(self, raw_data):
		''' 
			Converts the raw_data and saves the information into self.__status 
		''' 
		if len(raw_data) != self.__nr_bytes_requested:
			raise wrongDataFormatException(f"The recieved raw data has a length of {len(raw_data)} but the expected length is {self.__nr_bytes_requested}")
		
		self.__last_msg = raw_data
		command = raw_data[0]
		det_quality = raw_data[1]

		if command not in self.__accepted_commands:
				raise unknownCommandException(f"The command < {command} > sent from the device < {self.__name} > in the message < {raw_data} > is not in the list of accepted commands {self.__accepted_commands}")
				
		if det_quality > max(self.__range_detection_quality) or det_quality < min(self.__range_detection_quality):
			raise wrongDataFormatException(
				f"The detection quality has value of {det_quality}, but should be in range of {min(self.__range_detection_quality)} to {max(self.__range_detection_quality)} as defined in the config file for the device <{self.__name}>."
            )
		
		self.__set_current_to_last_status()          
		self.__status["command"] = command
		self.__status["detection_quality"] = det_quality
		self.__target["start_emg_recognition"] = False # reset the target to not start emg recognition


	def __send_command(self, message):
		''' 
			Sends a message to the device via I2C
			message: The message to send. It includes 2 bytes, the first is if it has to start emg recognition or not and the
			second is if each of the 4 vibration motors should be activated or not. The second byte has th following format:
			Bits 7-6: Motor 1 active or not
			Bits 5-4: Motor 2 active or not
			Bits 3-2: Motor 6 active or not
			Bits 1-0: Motor 7 active or not
		'''
		self.__i2c.write_bytes(self.__i2c_offset,message)




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

	

	def update_vibration_motors(self, values_dict):
		''' 
			Updates the status of the vibration motors based on the values_dict
			values_dict: A dictionary with keys "motor1", "motor2", "motor6", "motor7" and boolean values indicating if the motor 
			should be activated or not. If a motor is not in the dictionary, it remains unchanged.
			Example: {"motor1": True, "motor6": False}
		'''
		for key in values_dict.keys():
			if key not in self.__vibration_motors_strings:
				raise wrongDataFormatException(f"The key < {key} > is not a valid vibration motor key. Valid keys are: {list(self.__vibration_motors_strings)}")
			else:
				self.__target[key] = values_dict[key]


	def check_start_recognition(self):
		'''
			Sets the target to start emg recognition
		'''
		self.__target["start_emg_recognition"] = True

	
	def send_command(self):
		'''
			Sends the target to the emg device via I2C.
		'''
		message = []
		vibration_information = 0b00000000 # 8 bits for the vibration motors. Bits 7-6 for motor1, 5-4 for motor2, 3-2 for motor6 and 1-0 for motor7

		if self.__target["start_emg_recognition"] == True:
			message.append(0x2)
		else:
			message.append(0x0)
		
		if self.__target["motor1"] == True:
			vibration_information |= 0b01000000 # set bits 7-6 to 1
		else:
			vibration_information &= (~0b11000000) # set bits 7-6 to 0
		
		if self.__target["motor2"] == True:
			vibration_information |= 0b00010000 # set bits 5-4 to 1
		else:
			vibration_information &= (~0b00110000) # set bits 5-4 to 0
		
		if self.__target["motor6"] == True:
			vibration_information |= 0b00000100 # set bits 3-2 to 1
		else:
			vibration_information &= (~0b00001100) # set bits 3-2 to 0
		
		if self.__target["motor7"] == True:
			vibration_information |= 0b00000001 # set bits 1-0 to 1
		else:
			vibration_information &= (~0b00000011) # set bits 1-0 to 0

		message.append(vibration_information)
		self.__send_command(message)


	def print_status(self):
		''' 
			Prints the current status of the emg device to the console
		'''
		print(f"The status of <{self.__name}> is {self.__status}")

	
	def print_target(self):
		''' 
			Prints the current target with the command from "self.__target" to the console.
		'''
		print("The current target is:",self.__target)

	
	def print_config_data(self):
		''' 
			Prints the parameters and commands that were been loaded from the config file.
		'''
		print(f"The commands are: {self.__accepted_commands}")
		print(f"The range of detection quality is: {self.__range_detection_quality}")
		print(f"The I2C communication parameters are: i2c addr={self.__i2c_adr}; i2c offset={self.__i2c_offset}; nr bytes requested={self.__nr_bytes_requested}; nr bytes sent={self.__nr_bytes_sent}; max attempts={self.__max_attempts}")


	def get_status(self):
		''' 
			Returns the status of the command device 
		'''
		return self.__status


	def get_target(self):
		''' Returns the target command of the emg '''
		return self.__target


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
			print(f"I2C communication with emg closed.")
		else:
			print("No I2C communication to close.")


''' 
	Testing area
'''
if __name__ == "__main__":
	test = EmgDevice("emg_device","config.ini")
	test.print_config_data()
	sleep(3)

	'''
	test.check_start_recognition()
	test.print_target()
	test.send_command()
	sleep(6)
	test.update()
	test.print_status()
	'''

	'''
	test.update_vibration_motors({"motor2": True})
	test.print_target()
	test.send_command()
	sleep(10)

	test.update_vibration_motors({"motor2": False})
	test.print_target()
	test.send_command()
	sleep(10)
	'''

	
	test.update_vibration_motors({"motor1": True, "motor7": True})
	test.print_target()
	test.send_command()
	sleep(5)

	test.check_start_recognition()
	test.update_vibration_motors({"motor1": False, "motor7": False})
	test.print_target()
	test.send_command()
	sleep(5)
	test.update()
	test.print_status()
	
	
    