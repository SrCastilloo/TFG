import board
import neopixel
import time
import traceback
from configparser import ConfigParser
import RPi.GPIO as GPIO


class RgbControl:
	'''
		Description: This class represents the RGB LED control on the PCB.
		It allows to control the RGB LEDs connected to the Raspberry Pi.
		Version: 1.0
		Author: Práxedes Neira
	'''

	def __init__(self, name, conf_file) -> None:
		'''
			Init of the class RgbControl
			conf_file: Should include the path for the config file
			name: Name of the device, this has to correspond with the subsection name of the config file
		'''

		self.__name = name

		self.__pin_rgb = None
		self.__num_rgbs = None
		self.__brightness = None
		self.__ORDER=neopixel.RGB

		self.__pixels = None


		''' Load config file '''
		self.__config = ConfigParser()

		''' Load the config data'''
		try:
			self.__config.read(conf_file)           # read the config file
			self.__load_config()                    # load config data
			self.__pixels = neopixel.NeoPixel(board.D13, self.__num_rgbs, brightness=self.__brightness, auto_write=False, pixel_order=self.__ORDER)
		except KeyError as e:
			print(traceback.format_exc(), f"There has been a KeyError in the config file. Check the config file and the path.\nKey: {str(e)} does not exist.")
		except OSError as e:
			print(traceback.format_exc(),f"Remote I/O error occurred: {str(e)}")
		except Exception as e:
			print(traceback.format_exc(), f"An error has occurred: {str(e)}")
		else:
			print(f"RGB object with name {self.__name} sucessfully initialised.")


	def delete_pixels(self):
		'''
			Destroys the object __pixels to avoid memory leaks
		'''
		del self.__pixels


	'''
		Private methods of the class
	'''
	def __load_config(self):
		'''
			Loads the necessary data and information from the config file into the class 
		'''
		
		self.__pin_rgb = self.__config.getint(self.__name,'pin_led_rgb')
		self.__num_rgbs = self.__config.getint(self.__name,'num_leds_rgb')
		self.__brightness = self.__config.getfloat(self.__name,'brightness')

	
	'''
		Public methods of the class
	'''
	def set_color(self, color):
		'''
			Sets the color of the RGB LED.
			color: A tuple of (G, R, B) values.
		'''
		if self.__pixels is not None:
			self.__pixels.fill(color)
			self.__pixels.show()
		else:
			print("RGB pixels not initialized.")


	def set_brightness(self, brightness):
		'''
			Sets the brightness of the RGB LED.
			brightness: A float value between 0.0 and 1.0.
		'''
		if self.__pixels is not None:
			self.__pixels.brightness = brightness
			self.__pixels.show()
		else:
			print("RGB pixels not initialized.")

	
	def get_name(self):
		''' Returns the name of the device '''
		return self.__name
	

	def get_color(self):
		'''
			Returns the current color of the RGB LED.
			Returns: A tuple of (G, R, B) values.
		'''
		if self.__pixels is not None:
			return self.__pixels[0]
		else:
			print("RGB pixels not initialized.")
			return (0, 0, 0)
		
		
	def get_brightness(self):
		'''
			Returns the current brightness of the RGB LED.
			Returns: A float value between 0.0 and 1.0.
		'''
		if self.__pixels is not None:
			return self.__pixels.brightness
		else:
			print("RGB pixels not initialized.")
			return 0.0


	def clear(self):
		'''
			Clears the RGB LED (turn off).
		'''
		if self.__pixels is not None:
			self.__pixels.fill((0, 0, 0))
			self.__pixels.show()
		else:
			print("RGB pixels not initialized.")



''' 
	Testing area
'''
if __name__ == "__main__":
	conf_file = "config.ini"
	name = "rgb_control"
	test = RgbControl(name, conf_file)

	test.set_color((255, 0, 0))
	time.sleep(2)

	test.set_color((0, 255, 0))
	time.sleep(2)

	test.set_color((0, 0, 255))
	time.sleep(2)

	test.set_color((255, 255, 255))
	time.sleep(2)

	test.clear()