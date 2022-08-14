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

To get the output table, just run each function one by one. The score of each category is between 0 and 1. The highier the value is, the more subiable the resume is. 

Due to the time limit, i designed a simple model. The score of each resumes in a certain category (e.g., testing, development and management) consisted by three related parts (e.g., skills, degree and designations). The model assumes the three related parts have the same weights when scoring a resume in a given category. For example, when scoring a resume in testing, the model will count the sub-scores of skills, degree and designations, separately. Then the sum of the sub-scores is the final score of this given resume. For a sub-score, the model counts how many annotations of each resumes can match the related parts dictionary. Each resume will get a sub-score by using the counts devided by max sub-score. For example, a resume has two annotations realted to testing skills. In 220 resuems, the best resume have 10 annotations related to testing skills. Then the given resume will get a 2/10=0.2 as its sub-score in testing skills. Due to three related parts have the same weight, the sub-score need to deveid by 3. Therefore, 2/10/3 will be the final testing sub-score of this resume. After the model calculating all the three related parts sub-score of a resume, the sum of these three sub-score will be the final score of this resume in a certain category.
