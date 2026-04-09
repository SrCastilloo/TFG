from configparser import ConfigParser
import subprocess
import traceback
import RPi.GPIO as GPIO 


class SpeakerControl:
	'''
		Description: This class represents the speaker control on the PCB.
		It allows to control the speaker connected to the Raspberry Pi.
		Version: 1.0
		Author: Práxedes Neira
	'''

	def __init__(self, name, conf_file) -> None:
		'''
			Init of the class SpeakerControl
			conf_file: Should include the path for the config file
			name: Name of the device, this has to correspond with the subsection name of the config file
		'''

		self.__name = name

		self.__handModeSound = None
		self.__voiceModeSound = None
		self.__cameraModeSound = None

		''' Load config file '''
		self.__config = ConfigParser()

		''' Load the config data'''
		try:
			self.__config.read(conf_file)           # read the config file
			self.__load_config()                    # load config data
		except KeyError as e:
			print(traceback.format_exc(), f"There has been a KeyError in the config file. Check the config file and the path.\nKey: {str(e)} does not exist.")
		except OSError as e:
			print(traceback.format_exc(),f"Remote I/O error occurred: {str(e)}")
		except Exception as e:
			print(traceback.format_exc(), f"An error has occurred: {str(e)}")
		else:
			print(f"Speaker object with name {self.__name} sucessfully initialised.")

	
	'''
		Private methods of the class
	'''
	def __load_config(self):
		'''
			Loads the necessary data and information from the config file into the class 
		'''
		
		self.__handModeSound = self.__config.get(self.__name,'routeHandSound')
		self.__voiceModeSound = self.__config.get(self.__name,'routeVoiceSound')
		self.__cameraModeSound = self.__config.get(self.__name,'routeCameraSound')

		
	
	'''
		Public methods of the class
	'''
	def play_hand_sound(self):
		p = subprocess.run(["sudo", "mpg123", self.__handModeSound])

	
	def play_voice_sound(self):
		p = subprocess.run(["sudo", "mpg123", self.__voiceModeSound])


	def play_camera_sound(self):
		p = subprocess.run(["sudo", "mpg123", self.__cameraModeSound])

	

''' 
	Testing area
'''
if __name__ == "__main__":
	conf_file = "config.ini"
	name = "speaker_control"
	test = SpeakerControl(name, conf_file)

	pin_shutdown_speaker = 14 # GPIO pin for not shutting down speaker

	GPIO.cleanup() # Reset GPIO config if existing
	GPIO.setmode(GPIO.BCM) # set the used pin numbers to the Broadcom SOC channel numbers
	GPIO.setwarnings(False) # disable the warnings
	GPIO.setup(pin_shutdown_speaker, GPIO.OUT, initial=GPIO.HIGH) # Init of the wake up pin

	test.play_hand_sound()

	test.play_voice_sound()

	test.play_camera_sound()

	GPIO.output(pin_shutdown_speaker, GPIO.LOW) 