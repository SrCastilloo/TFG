from .i2cdevice import I2cDevice
from configparser import ConfigParser
import traceback
from time import sleep


''' 
	Custom exceptions for this class
'''
class positionNotFoundException(Exception):
	"The position number was not found. Please check that the position exist"
	pass

class wrongDataFormatException(Exception):
	"The received raw data has not the correct format."
	pass

class unknownCommandException(Exception):
	"The command sent cannot be found."
	pass

class commandsDifferException(Exception):
	"You can only use one command at the same time."
	pass

class unknownFingerException(Exception):
	"The finger you want to adress does not exist."
	pass

class unknownNumberOfFingersException(Exception):
	"The Number of fingers to adress is unknown."
	pass



class HandControl:
	''' class handControl
		This class represents the control module of the robotic hand, it comunicates directly with the motor control microcontroller via I2C.
		This class is able to initlialize itself with a provided config file, which is introduced to the class constructor.

		Version:	2.0
		Author:		Kevin Vogel, Práxedes Neira
	'''

	''' Constants of the class '''
	__mask_msb = 0xFF00
	__mask_lsb = 0x00FF

	''' Commands and constants for the I2C communication '''
	__cmd_read = None
	__cmd_stop = None
	__cmd_move = None
	__cmds = []
	__nr_cmd_bytes = None
	__nr_status_bytes = None
	__nr_bytes_per_finger = None
	__nr_used_fingers = None
	__nr_bytes_requested = None
	__nr_bytes_to_write = None
	__cmds_dct = {"stop": None,"move": None, "stop_individual" : None,"command": None}
	__finger_strings = None

	''' Commands and constants for the I2C communication '''
	__finger_max = {"ring" : None, "middle" : None, "index" : None, "thumb0" : None, "thumb1" : None}
	__finger_min = {"ring" : None, "middle" : None, "index" : None, "thumb0" : None, "thumb1" : None}  

	''' 
		Private methods of the class
	'''

	def __init__(self,conf_file) -> None:
		'''
			Init of the class handControl
			conf_file should include the path for the config file
		'''
		''' Positions and status of the hand '''
		self.__status = {"status": None, "ring": None, "middle": None, "index": None, "thumb0": None, "thumb1": None}

		''' Target position and command for the hand movement '''
		self.__target = {"command": None, "ring": None, "middle": None, "index": None, "thumb0": None, "thumb1": None}

		''' Contains the predefined positions. Array of dictionaries with the keys: nr, ring, middle, index, thumb0, thumb1'''
		self.__positions = []

		''' I2C communication information '''
		self.__i2c_offset = None 

		''' Load config file '''
		self.__config = ConfigParser()
		

		''' Init of i2c, load the config data and read status of the hand'''
		try:
			self.__config.read(conf_file) # read the config file 
			self.__i2c_adr = int(self.__config['mc_hand']['i2c_adress'],16) # adress is given in hex so convert it
			self.__i2c = I2cDevice(self.__i2c_adr)
			self.__load_config()
			self.update_status()
			self.__set_target_to_status()
		except KeyError as e:
			print(traceback.format_exc(), f"There has been a KeyError in the config file. Check the config file and the path.\nKey: {str(e)} does not exist.")
		except OSError as e:
			print(traceback.format_exc(),f"Remote I/O error occurred: {str(e)} \nCheck I2C connection and address.")
		except Exception as e:
			print(traceback.format_exc(), f"An error has occurred: {str(e)}")
		else:
			print("HandControl sucessfully initialised.")



	def __load_config(self):
		''' 
			Loads the necessary data and information from the config file into the class 
		'''

		''' Load i2c commands '''
		self.__cmd_read = self.__config.getint('mc_hand','cmd_read')
		self.__cmds.append(self.__cmd_read)
		self.__cmd_stop = self.__config.getint('mc_hand','cmd_stop')
		self.__cmds.append(self.__cmd_stop)
		self.__cmds_dct["stop"] = self.__cmd_stop
		self.__cmd_move = self.__config.getint('mc_hand','cmd_move')
		self.__cmds.append(self.__cmd_move)
		self.__cmds_dct["move"] = self.__cmd_move
		self.__cmds_dct["stop_individual"] = self.__config.getint('mc_hand','cmd_individual_stop')

		''' Load communication format/information'''
		self.__i2c_offset = self.__config.getint('mc_hand','i2c_offset')
		self.__nr_bytes_per_finger = self.__config.getint('mc_hand','nr_bytes_per_finger')
		self.__nr_cmd_bytes = self.__config.getint('mc_hand','nr_cmd_bytes')
		self.__nr_status_bytes = self.__config.getint('mc_hand','nr_status_bytes')
		self.__nr_used_fingers = self.__config.getint('mc_hand','nr_used_fingers')
		if self.__nr_used_fingers == 5:
			self.__finger_strings = ["ring", "middle", "index", "thumb0", "thumb1", "command"]
		else:
			raise unknownNumberOfFingersException
		self.__nr_bytes_requested = self.__config.getint('mc_hand','nr_bytes_send_recieve')
		self.__nr_bytes_to_write = self.__config.getint('mc_hand','nr_bytes_send_recieve')

		''' Load maximum and minimum position values '''
		self.__finger_max['ring'] = self.__config.getint('minMaxValues','ring_max')
		self.__finger_min['ring'] = self.__config.getint('minMaxValues','ring_min')
		self.__finger_max['middle'] = self.__config.getint('minMaxValues','middle_max')
		self.__finger_min['middle'] = self.__config.getint('minMaxValues','middle_min')
		self.__finger_max['index'] = self.__config.getint('minMaxValues','index_max')
		self.__finger_min['index'] = self.__config.getint('minMaxValues','index_min')
		self.__finger_max['thumb0'] = self.__config.getint('minMaxValues','thumb0_max')
		self.__finger_min['thumb0'] = self.__config.getint('minMaxValues','thumb0_min')
		self.__finger_max['thumb1'] = self.__config.getint('minMaxValues','thumb1_max')
		self.__finger_min['thumb1'] = self.__config.getint('minMaxValues','thumb1_min') 

		''' Load command '''

		''' Load positions from config file '''
		self.__load_positions_from_conf()



	def __parse_raw_data(self, raw_data):
		''' 
			Parses the raw data recieved via I2c from the motor control microcontroller into the dictionary "self.__status"
			This method is written for 5 fingers so in total 11 bytes, for other formats the code need to be changed. 
		'''
		len_raw_data = len(raw_data)
		if len_raw_data != self.__nr_bytes_requested:
			raise wrongDataFormatException(f"The recieved raw data from the motor microcontroller has the wrong length.\nThe expectetd number of bytes is {self.__nr_bytes_requested} but recieved where {len_raw_data}")
		self.__status["status"] = raw_data[0]
		self.__status["ring"] = raw_data[1] << 8| raw_data[2] 
		self.__status["middle"] = raw_data[3] << 8| raw_data[4]
		self.__status["index"] = raw_data[5] << 8| raw_data[6] 
		self.__status["thumb0"] = raw_data[7] << 8| raw_data[8]
		self.__status["thumb1"] = raw_data[9] << 8| raw_data[10] 


	def __set_target_to_status(self):
		''' 
			Sets the target to the current status
		'''
		for key in self.__status:
			if key in self.__target.keys():
				self.__target[key] = self.__status[key]


	def __convert_target_information(self):
		''' 
			Converts the target information of self.__target into a I2C message to send to the motor microcontroller.
			Data format: 
				First byte = command byte
				Next bytes = two bytes for each finger/motor to control MSB first
		'''
		print(f"From hand control target=",self.__target)
		data = []
		data.append(self.__target["command"])
		data.append((self.__target["ring"] & self.__mask_msb) >> 8)
		data.append(self.__target["ring"] & self.__mask_lsb)

		''' The middle finger is not used right now, so we set it to 5000, which is the stop command for the middle finger '''
		#data.append((self.__target["middle"] & self.__mask_msb) >> 8)
		#data.append(self.__target["middle"] & self.__mask_lsb)
		data.append((5000 & self.__mask_msb) >> 8)
		data.append(5000 & self.__mask_lsb)

		data.append((self.__target["index"] & self.__mask_msb) >> 8)
		data.append(self.__target["index"] & self.__mask_lsb)
		data.append((self.__target["thumb0"] & self.__mask_msb) >> 8)
		data.append(self.__target["thumb0"] & self.__mask_lsb)                   
		data.append((self.__target["thumb1"] & self.__mask_msb) >> 8)
		data.append(self.__target["thumb1"] & self.__mask_lsb)
		return data



	def __load_positions_from_conf(self):
		''' 
			This method loads the predefined positions from the config file.

			The positions should be in the area 'positions' defined
			DATA FORMAT: pX_FINGER = VALUE
			X: represents the position number
			FINGER: represents the corresponding finger with an element of {ring, middle, index, thumb0, thumb1} 
		'''
		loc_dct = {}
		last_pos = None
		for key in list(self.__config['positions'].keys()):
			pos_nr = int(key[1]) # extract the second char from the key index        
			if pos_nr != last_pos and self.__check_dict(loc_dct):
				self.__positions.append(loc_dct)
				loc_dct = {}
			loc_dct["nr"] = pos_nr
			
			if "ring" in key:
				loc_dct['ring'] = self.__config.getint('positions',key)
			elif "middle" in key:
				loc_dct['middle'] = self.__config.getint('positions',key)
			elif "index" in key:
				loc_dct['index'] = self.__config.getint('positions',key)
			elif "thumb0" in key:
				loc_dct["thumb0"] = self.__config.getint('positions',key)
			elif "thumb1" in key:
				loc_dct["thumb1"] = self.__config.getint('positions',key)
			last_pos = pos_nr
		if self.__check_dict(loc_dct): # append the last position that is been loaded
			self.__positions.append(loc_dct)



	def __check_dict(self, dictionary):
		''' 
			Checks if the dictionary that is handed over, contains seven elements 
		'''
		if len(dictionary.keys()) == self.__nr_used_fingers + 1: # number of finger positions + position number
			return 1
		else:
			return 0
		


	def __get_swap_dict(self, d):
		''' 
			Swaps keys and values of a dict and returns it
		'''
		return {v: k for k, v in d.items()}    


	def __check_command_dict(self, cmd, cmd_dict):
		''' 
			Checks a given dictionary if commands are known, also translates the stop and move commands for each finger 
		'''
		swap_dict = self.__get_swap_dict(self.__cmds_dct)
		for key in cmd_dict:
			if key != "command":
				if cmd_dict[key] != swap_dict[cmd]:
					raise commandsDifferException(f"key={cmd_dict[key]} type:{type(cmd_dict[key])}; given command={cmd} type:{type(cmd)}")
				if key not in self.__finger_strings:
					raise unknownFingerException(f"The finger: {key} does not exist.")
				if cmd_dict["command"] == self.__cmds_dct["stop"]:
					cmd_dict[key] = self.__cmds_dct["stop_individual"]
		return True


	def __check_command_dict_on_off(self, cmd, cmd_dict):
		''' 
			Checks a given dictionary if commands are known, also translates the open and close commands for each finger 
		'''
		swap_dict = self.__get_swap_dict(self.__cmds_dct)
		if cmd not in swap_dict.keys():
			raise commandsDifferException
		for key in cmd_dict:
			if key != "command":
				if key not in self.__finger_strings:
					raise unknownFingerException(f"The finger: {key} does not exist.")
				if cmd_dict[key] == "open":
					cmd_dict[key] = self.__finger_max[key]
				elif cmd_dict[key] == "close":
					cmd_dict[key] = self.__finger_min[key]
				else:
					raise unknownCommandException(f"Command {cmd_dict[key]} was not found.")
		

	def __update_target(self, cmd_dict):
		''' 
			Updates the target dictionary with the given command dictionary
		'''
		for key in cmd_dict:
			if key == "command":
				self.__target["command"] = cmd_dict["command"] # set the command in the target
			if key in self.__finger_strings:
				self.__target[key] = cmd_dict[key]
			else:
				raise unknownFingerException(f"Unkown finger: {key}; type={type(key)}")



	def __load_target_position(self,nr_target,command):
		'''
			Loads the target position with the number "nr_target" into the dictionary "self.__target" with the command "command"
		'''
		if command not in self.__cmds:
			raise unknownCommandException(f"The command sent with the nr {command} cannot be found.")
		for dct in self.__positions: # iterate over each dictionary in __positions
			if dct["nr"] == nr_target:
				self.__target = {
					"command": command,
					"ring": dct["ring"],
					"middle": dct["middle"],
					"index": dct["index"],
					"thumb0": dct["thumb0"],
					"thumb1": dct["thumb1"]
				}
				return
		raise positionNotFoundException(f"Position with the number {nr_target} could not be resolved. Check that the position you want to move exist.")
		



	def __check_target_position(self):
		''' 
			Checks if all values in target dictionary are integer values. 
		'''
		keys = list(self.__target.keys())
		i = 0
		while i < len(keys) and isinstance(self.__target[keys[i]],int):
			i += 1
		if i == len(keys):
			return 1
		else:
			return 0
	

	def __send_command_target(self, nr_target, command):
		'''
			Sends to the motor controller microcontroller the target information corresponding to the target number "nr_target" and the command with the command number "command"
		'''
		self.__load_target_position(nr_target,command)
		if self.__check_target_position():
			data = self.__convert_target_information()
			self.__i2c.write_bytes(self.__i2c_offset,data)
		else:
			print("The target position is not correct. Please check the values in the target position dictionary.")
			pass


	def __send_command(self):
		'''
			Converts the target information and send the command via I2C to the motor controler
		'''
		message = self.__convert_target_information()
		self.__i2c.write_bytes(self.__i2c_offset,message)
		


	def __copy_dct(self, dct):
		''' Copies a given dictionary and returns it '''
		loc_dct = {}
		for key in dct:
			loc_dct[key] = dct[key]
		return loc_dct


	''' 
		Public methods of the class
	'''
	def update_status(self):
		'''
			Reads the status from the motor control microcontroller via I2C and put the data into "self.__status". (For printing the status use print_status())
		'''
		raw_data = self.__i2c.read_bytes(self.__i2c_offset, self.__nr_bytes_requested)
		self.__parse_raw_data(raw_data)


	def move_open_close(self, cmd_values):
		''' 
			Moves the hand in an binary way so every finger can be open and closed between the minimum and maximum value. The command doesnt have to include all finger keys. 
			< cmd_values > should be an dictionary:
			Accepted keys = [ "ring", "middle", "index", thumb0", "thumb1"]
			Accepted key values = [ "open", "close"]
			Example command values: cmd_values = {"middle":"close", "index":"open", "thumb0": "close", "thumb1":"open"} 
			=> ring finger do nothing, middle finger to minimum position, index finger to maximum position, thumb0 finger to minimum position and thumb1 to maximum position
		'''
		cmd_values["command"] = self.__cmds_dct["move"] # add the move command
		self.__check_command_dict_on_off(self.__cmds_dct["move"], cmd_values)
		self.__update_target(cmd_values)
		self.__send_command()


	def move_position(self, position_nr):
		''' 
			Moves the hand to a predefined position with the number "position_nr"
		'''
		self.__send_command_target(position_nr, self.__cmd_move)


	def stop_movement(self,cmd_values):
		''' 
			Sends the stopp command to the motor control microcontroller via I2C
			
			< cmd_values > should be a dictionary:
			Accepted keys = [ "ring", "middle", "index", thumb0", "thumb1"]
			Accepted key values = [ "stop" ] 
			Example command values: cmd_values = {"middle":"stop", "index":"stop", "thumb1":"stop"}
			=> ring finger do nothing, middle finger stop movement, index finger stop movement, thumb0 finger do nothing and thumb1 stop movement
		'''
		cmd_values["command"] = self.__cmds_dct["stop"] # add the stopp command to the given command values
		self.__check_command_dict(self.__cmds_dct["stop"], cmd_values) # checks the given command values
		self.__update_target(cmd_values) # updates the target with the given command values 
		self.__send_command() # send the new command 


	def print_status(self):
		''' 
			Prints the status information from "self.__status" to the console.
		'''
		print("Current status and positions are:", self.__status)


	def print_positions(self):
		''' 
			Prints the saved positions from "self.__positions" to the console.
		'''
		print("Defined positions are:",self.__positions)


	def print_target_position(self):
		''' 
			Prints the current target position with the command from "self.__target" to the console.
		'''
		print("The current target position is:",self.__target)


	def print_config_data(self):
		''' 
			Prints the parameters and commands that were been loaded from the config file.
		'''
		print(f"The commands are: read={self.__cmd_read}; stop={self.__cmd_stop}; move={self.__cmd_move};")
		print(f"The I2C communication parameters are: i2c addr={self.__i2c_adr}; i2c offset={self.__i2c_offset}; nr bytes per finger={self.__nr_bytes_per_finger}; nr cmd bytes={self.__nr_cmd_bytes}; nr status bytes={self.__nr_status_bytes}; nr used fingers/motors={self.__nr_used_fingers}; nr bytes requested={self.__nr_bytes_requested}, nr bytes to write={self.__nr_bytes_to_write }")


	def get_position(self, number):
		'''
			Returns the position dictionary corresponding to the number. If number not found returns None.
		'''
		counter = 0
		for dict in self.__positions:
			if dict["nr"] == number:
				break
			counter += 1
		
		if counter == len(self.__positions): # the position is not found
			return None
		else: 
			loc_dict = self.__copy_dct(self.__positions[counter])
			loc_dict.pop("nr") # delete number entry
			return loc_dict
		

	def get_target(self):
		''' Returns the target position of the hand '''
		return self.__target


	def get_status(self):
		''' Returns the current positions of the hand '''
		return self.__status


	def close_i2c(self):
		''' 
			Closes the I2C communication
		'''
		if self.__i2c is not None:
			self.__i2c.close()
			print(f"I2C communication with mc closed.")
		else:
			print("No I2C communication to close.")


''' 
Testing area
'''
if __name__ == "__main__":
	conf_file = "config.ini"
	test = HandControl(conf_file)
	sleep(3) # Wait for the microcontroller to start
	test.update_status()
	test.print_config_data()
	test.print_positions()
	test.print_status()
	test.print_target_position()

	
	test.move_position(8)
	sleep(5)
	test.update_status()
	test.print_status()
	sleep(2)
	test.move_position(0)
	sleep(5)
	test.update_status()
	test.print_status()
	sleep(2)
	#stopp_cmd = {"ring":"stop", "middle":"stop", "index":"stop", "thumb0": "stop", "thumb1":"stop"}
	stopp_cmd = {"ring":"stop", "middle":"stop"}
	test.stop_movement(stopp_cmd)
	
	
	
	'''
	while True:
		test.update_status()
		test.print_status()
		sleep(2)
	'''
	
	
	'''
	#cmd_open_close0 = {"ring":"open", "middle":"close", "index":"open", "thumb0": "close", "thumb1":"open"}
	#cmd_open_close1 = {"ring":"close", "middle":"open", "index":"close", "thumb0": "open", "thumb1":"close"}
	cmd_open_close2 = {"ring":"close", "middle":"close", "index":"close", "thumb0": "close", "thumb1":"close"}
	cmd_open_close3 = {"ring":"open", "middle":"open", "index":"open", "thumb0": "open", "thumb1":"open"}
	#test.move_open_close(cmd_open_close0)
	test.move_open_close(cmd_open_close2)
	sleep(8) 
	test.update_status()
	test.print_status()
	#test.move_open_close(cmd_open_close1)
	test.move_open_close(cmd_open_close3)
	sleep(8)
	test.update_status()
	test.print_status()
	# Stops all fingers
	stopp_cmd = {"ring":"stop", "middle":"stop", "index":"stop", "thumb0": "stop", "thumb1":"stop"}
	# Stops only one finger. The others must continue moving
	#stopp_cmd = {"ring":"stop"}    
	test.stop_movement(stopp_cmd) # is working
	sleep(1)
	test.update_status()
	test.print_status()
	'''

  