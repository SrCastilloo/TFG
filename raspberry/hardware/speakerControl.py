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

		# NUEVO: pin que despierta el amplificador del altavoz
		self.__pin_shutdown_speaker = None

		''' Load config file '''
		self.__config = ConfigParser()

		''' Load the config data'''
		try:
			self.__config.read(conf_file)           # read the config file
			self.__load_config()                    # load config data

			# NUEVO: activar el amplificador para que el altavoz suene
			self.__init_speaker_gpio()

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

		# NUEVO: cargar el pin desde config.ini
		self.__pin_shutdown_speaker = self.__config.getint('GPIO', 'pin_shutdown_speaker')


	def __init_speaker_gpio(self):
		'''
			Initializes the GPIO pin that keeps the speaker amplifier enabled.
			If this pin is LOW, mpg123 decodes the audio but nothing is heard.
		'''

		GPIO.setmode(GPIO.BCM)
		GPIO.setwarnings(False)
		GPIO.setup(self.__pin_shutdown_speaker, GPIO.OUT, initial=GPIO.HIGH)

		print(f"Speaker shutdown pin GPIO{self.__pin_shutdown_speaker} set to HIGH.")

		
	
	'''
		Public methods of the class
	'''
	def play_hand_sound(self):
		p = subprocess.run(["sudo", "mpg123", self.__handModeSound])

	
	def play_voice_sound(self):
		p = subprocess.run(["sudo", "mpg123", self.__voiceModeSound])


	def play_camera_sound(self):
		p = subprocess.run(["sudo", "mpg123", self.__cameraModeSound])


	def shutdown_speaker(self):
		'''
			Shutdown the speaker amplifier.
		'''

		if self.__pin_shutdown_speaker is not None:
			GPIO.output(self.__pin_shutdown_speaker, GPIO.LOW)

	

''' 
	Testing area
'''
if __name__ == "__main__":
	conf_file = "config.ini"
	name = "speaker_control"
	test = SpeakerControl(name, conf_file)

	test.play_hand_sound()

	test.play_voice_sound()

	test.play_camera_sound()

	test.shutdown_speaker()