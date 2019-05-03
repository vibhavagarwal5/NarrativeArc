class LearningResource:
	def __init__(self, sequence_id, resource_id, title, description, text):
		self.sequence_id = sequence_id #Id number in the collection
		self.resource_id = resource_id 
		self.title = title
		self.description = description
		self.text = text  # Cleaned up version of concat(title,description)

class Collection:

	def __init__(self, id):
		self.id = id
		self.learning_resources = []

	def add_learning_resource(self, learning_resource):
		self.learning_resources.append(learning_resource)
