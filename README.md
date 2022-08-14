# KTP Exercise HR GO
Function get_clean_text is used to trim and clean the special characters in text content and return a cleaned text content list.
Function get_labels is used to get labels of annotations and return a list of all the labels.
Function get_single_annotation_list is used to get all the annotations with a certain label. For example, get_single_annotation_list(data['annotation'], 'Designation') will return a list contains all the designations.
Function get_individual_annotations is used to remove all the special characters and duplicate annotations, and seprate the annotations into individual skills.
Function conver_list is used to get all the unique items in the annotations lists, which can help me to generate dictionary.
Dictionaries degree_dict, designation_dict and skill_dict contains all the labels realted to testing, development and management. All the value of dictionaries are extracted from annotation labels.
Function cal_score is used to get the basic score of each resumes of a certain category, which will be used to calculate the final score of each resumes of a certain category.
Function get_score is used to caluate final score of each resumes of all the categories (e.g., testing, development and management).
Function output_results is used to output the tabel of the index of the resume and the three values for testing, development and management.
To get the output table, just run each function one by one.
