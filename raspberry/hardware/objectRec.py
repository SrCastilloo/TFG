import cv2
import threading
import traceback
from time import sleep, time
from configparser import ConfigParser


class ObjectRec:
	''' class ObjectRec
		Description: This class represents the object recognition of the mechanical hand
		Version:	2.0
		Author:		Práxedes Neira
	'''

	def __init__(self, name, conf_file) -> None:
		'''
			Init of the class ObjectRec
			conf_file: Should include the path for the config file
			name: Name of the device, this has to correspond with the subsection name of the config file
		'''

		self.__name = name

		self.__accepted_objects = [] 
		self.__range_detection_quality = [] # Range of acceptable detection quality
		self.__status = {"object": None, "detection_quality": None}
		self.__last_status = {"object": None, "detection_quality": None}
		self.__best_status = {"object": None, "detection_quality": 0.0}
		self.__best_last_status = {"object": None, "detection_quality": 0.0}
		self.__running = False

		# Paths and settings for the object detection model
		self.__labels_path = None # Path of the labels of the object detection model
		self.__model_config_path = None # Path of the model configuration file
		self.__model_weights_path = None # Path of the model weights file
		self.__camera_index = 0 # Index of the camera to use
		self.__camera_width = 640 # Width of the camera frame
		self.__camera_height = 480 # Height of the camera frame

		




		''' Load config file '''
		self.__config = ConfigParser()

		''' Load the config data'''
		try:
			self.__config.read(conf_file)           # read the config file
			self.__load_config()                    # load config data
			self.__init_model_and_camera()          # initialize the object detection model and the camera
		except KeyError as e:
			print(traceback.format_exc(), f"There has been a KeyError in the config file. Check the config file and the path.\nKey: {str(e)} does not exist.")
		except OSError as e:
			print(traceback.format_exc(),f"Remote I/O error occurred: {str(e)}")
		except Exception as e:
			print(traceback.format_exc(), f"An error has occurred: {str(e)}")
		else:
			print(f"Object recognition with name {self.__name} sucessfully initialised.")


	def __del__(self):
		if hasattr(self, 'cap') and self.cap is not None:
			self.cap.release()
		cv2.destroyAllWindows()

		


	'''
		Private methods of the class
	'''
	def __load_config(self):
		'''
			Loads the necessary data and information from the config file into the class 
		'''

		''' Load array of accepted objects '''
		raw_value = self.__config[self.__name]['accepted_objects']
		self.__accepted_objects = [item.strip() for item in raw_value.split(',')] # accepted_objects = ["person", "cup", etc.]
		


		''' Load range of acceptable detection quality '''
		cmd = self.__config.getint(self.__name,'min_quality')
		self.__range_detection_quality.append(cmd)
		cmd = self.__config.getint(self.__name,'max_quality')
		self.__range_detection_quality.append(cmd)


		''' Load model paths'''
		self.__labels_path = self.__config.get(self.__name,'labels_path')
		self.__model_config_path = self.__config.get(self.__name,'model_config_path')	
		self.__model_weights_path = self.__config.get(self.__name,'model_weights_path')


	def __init_model_and_camera(self):
		'''
		Initializes the OpenCV model and camera using the config file paths 
		'''
		self.classNames = []

		with open(self.__labels_path, 'rt') as f:
			self.classNames = f.read().rstrip('\n').split('\n')
		
		self.net = cv2.dnn_DetectionModel(self.__model_weights_path, self.__model_config_path)
		self.net.setInputSize(320, 320)
		self.net.setInputScale(1.0 / 127.5)
		self.net.setInputMean((127.5, 127.5, 127.5))
		self.net.setInputSwapRB(True)
		
		self.cap = cv2.VideoCapture(self.__camera_index)
		self.cap.set(3, self.__camera_width)
		self.cap.set(4, self.__camera_height)



	def __set_current_to_last_status(self):
		''' 
			sets the current status "self.__status" to the last status "self.__last_status"
        '''
		for key in self.__status.keys():
			self.__last_status[key] = self.__status[key]



	''' 
		Public methods of the class
	'''
	def getObjects(self, img, thres, nms, draw=True, objects=[]):
		'''
			Detects objects in the provided image "img" with the provided thresholds "thres" and "nms"
			If "draw" is True, the detected objects are drawn in the image
			If "objects" is provided, only the objects in the list are detected
			Returns the image with the detected objects and a list of the detected objects
		'''
		classIds, confs, bbox = self.net.detect(img, confThreshold=thres, nmsThreshold=nms)
		if len(objects) == 0:
			objects = self.classNames
		objectInfo = []
		if len(classIds) != 0:
			for classId, confidence, box in zip(classIds.flatten(), confs.flatten(), bbox):
				className = self.classNames[classId - 1]
				if className in objects:
					objectInfo.append([box, className, confidence])
					if draw:
						cv2.rectangle(img, box, color=(0, 255, 0), thickness=2)
						cv2.putText(img, className.upper(),(box[0] + 10, box[1] + 30),
									cv2.FONT_HERSHEY_COMPLEX, 1, (0, 255, 0), 2)
						cv2.putText(img, str(round(confidence * 100, 2)), (box[0] + 200, box[1] + 30),
									cv2.FONT_HERSHEY_COMPLEX, 1, (0, 255, 0), 2)
		return img, objectInfo
	

	def update_status(self):
		'''
			Requests information from the camera and updates the status of the object recognition
		'''
		while self.__running:
			# Read various frames without processing, to discard the first ones and get the image updated and not with the previous image
			for _ in range(5):
				self.cap.read()
			success, img = self.cap.read()
			if success:
				result, objectInfo = self.getObjects(img, 0.45, 0.2, False, objects=self.__accepted_objects)
				if objectInfo:
					objectInfo.sort(key=lambda x: x[2], reverse=True)
					self.__set_current_to_last_status()
					self.__status["object"] = objectInfo[0][1] 
					self.__status["detection_quality"] = objectInfo[0][2] * 100
				else:
					self.__set_current_to_last_status()
					self.__status = {"object": None, "detection_quality": None}
				#cv2.imshow("Output", img)
				#cv2.waitKey(1)
			else:
				break


	def print_status(self):
		'''
			Prints the current status of the command device to the console
		'''
		print(f"The status of th object recognition is {self.__status}")
	

	def print_best_status(self):
		''' 
			Prints the current status of the command device to the console
		'''
		print(f"The status of th object recognition is {self.__best_status}")

	
	def print_config_data(self):
		''' 
			Prints the parameters and commands that were been loaded from the config file.
		'''   
		print(f"The accepted objects are: {self.__accepted_objects}")
		print(f"The range of acceptable detection quality is: {self.__range_detection_quality}")


	def get_status(self):
		''' 
			Returns the status of the device 
		'''    
		return self.__status
	

	def get_best_status(self):
		''' 
			Returns the best status of the device 
		'''    
		return self.__best_status
	

	def get_name(self):
		''' Returns the name of the device '''
		return self.__name
	

	def get_object(self):
		''' 
			Returns the object stored into status
		'''
		return self.__status["object"]


	def get_detection_quality(self):
		''' 
			Returns the detection quality (in %) stored into status
		'''
		return self.__status["detection_quality"]
	

	def get_best_object(self):
		'''
			Returns the object stored into the best status
		'''
		return self.__best_status["object"]


	def get_best_detection_quality(self):
		''' 
			Returns the detection quality (in %) stored into the best status
		'''
		return self.__best_status["detection_quality"]
	

	def get_running(self):
		'''
			Returns the running status of the object recognition
		'''
		return self.__running
		
	
	def new_data(self):
		'''
			Compares the values "self.__best_status["object"]" and "self.__best_last_status["object"]" and check if __best_status is not None
			Return "True" if the objects are diffrent between "self.__best_status" and "self.__best_last_status" and __best_status is not None else return "False"
		'''
		if self.__best_status["object"] != None and self.__best_status["detection_quality"] != 0.0 and self.__best_status["object"] != self.__best_last_status["object"]:
			return True
		else:
			return False 
	

	def status_not_none(self):
		'''
			Check if __status is not None
			Return "True" if __status is not None else return "False"
		'''
		if self.__status["object"] != None and self.__status["detection_quality"] != None:
			return True
		else:
			return False 
		

	def clear_status(self):
		''' 
			Clears the status and last status of the command device and the best status and last best status
		'''
		self.__status = {"object": None, "detection_quality": None}
		self.__last_status = {"object": None, "detection_quality": None}
		self.__best_status = {"object": None, "detection_quality": 0.0}
		self.__best_last_status = {"object": None, "detection_quality": 0.0}
		print(f"Status, last status and best status, best last status of <{self.__name}> cleared.")


	def on(self):
		''' 
			Starts the object recognition in a different thread
		'''
		self.__running = True
		self.thread = threading.Thread(target=self.update_status)
		self.thread.start()


	def off(self):
		'''
			Stopps the object recognition in a different thread
		'''
		self.__running = False
		self.thread.join()

	
	def detect_object(self):
		'''
			Starts the object detection for 2 seconds, and stores the best detected object and its quality in self.__best_status
		'''
		self.on()  # Init object detection
		print("Object detection started.")

		# update last best status
		self.__best_last_status["object"] = self.__best_status["object"]
		self.__best_last_status["detection_quality"] = self.__best_status["detection_quality"]

		start_time = time()
		self.__best_status = {"object": None, "detection_quality": 0.0}

		while time() - start_time < 2:  # Wait 2 seconds for the result
			status = self.get_status()
			if self.status_not_none():
				current_object = status["object"]
				current_quality = status["detection_quality"]

				print(f"Object detected: {current_object} with quality {current_quality:.2f}%")

				if current_quality > self.__best_status["detection_quality"]:
					self.__best_status["object"] = current_object
					self.__best_status["detection_quality"] = current_quality

			sleep(0.1)

		self.off()  # Stops object detection
		print("Object detection stopped.")

		if self.__best_status["object"] is not None:
			print(f"Best object detected: {self.__best_status['object']} with quality {self.__best_status['detection_quality']:.2f}%")
		else:
			print("No object detected.")



''' 
	Testing area
'''
if __name__ == "__main__":
	conf_file = "config.ini"
	name = "object_rec"
	test = ObjectRec(name, conf_file)
	test.print_config_data()
	sleep(3) # Wait 3s

	while True:
		test.detect_object()
		sleep(5)