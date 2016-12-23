Question Generation Shared Task & Evaluation Challenge (QGSTEC) 2010 - 
Generating Questions from Sentences
====================================================================


The data in this corpus are released under a Creative Commons Attribution-NonCommercial-ShareAlike 2.0 UK licence: England & Wales (http://creativecommons.org/licenses/by-nc-sa/2.0/uk/)


CONTENTS of QGSTEC-Sentences-2010.zip
=====================================

- README.txt
- QGSTEC_INLG2010Proceedings.pdf
- InstructionsParticipants_QGfromSentences.pdf
- DevelopmentData_QuestionsFromSentences.xml
- TestData_QuestionsFromSentences.xml
- evaluation.xsl
- TestDataSummary.xls


Short description
=================

A corpus of over 1000 questions (both human and machine generated). The automatically generated questions have been rated by several raters according to five criteria (relevance, question type, syntactic correctness and fluency, ambiguity, and variety).


Summary
=======

The 1st Question Generation Shared Task & Evaluation Challenge took place in 2010 as part of the Third Workshop on Question Generation (http://www.questiongeneration.org/QG2010/). The aim of the challenge was to advance research on Question Generation (QG). The challenge consisted of two tasks: (A) Generation of Questions from Paragraphs, and (B) Generation of Questions from Single Sentences. The tasks are described in more detail in QGSTEC_INLG2010Proceedings.pdf. A description of the five systems that participated in the tasks can be found in the Proceedings of QG Workshop (http://oro.open.ac.uk/22343/). The current corpus consists of the data resulting from task (B), Question Generation from Sentences (extracted from three resources: OpenLearn, Wikipedia and Yahoo Answers). The original description of this task for the participating teams is provided in InstructionsParticipants_QGfromSentences.pdf.


Task B: Generation from Sentences
=================================

Systems that took part in this task were required to generate questions from single sentences. The participating teams were provided with an initial development data set (DevelopmentData_QuestionsFromSentences.xml) indicative of test data (TestData_QuestionsFromSentences.xml). 

Both development and test data are in XML format. For the test data, the XML schema consisted of a root 'dataset' element with a child element called 'instance' for each test instance. The 'text' element contained
the input to the QG system and the 'targetQuestionType' element specified the question type to be generated. Participant's submissions (anonymised as a, b, c, and d) are in the 'submission' element with the corresponding ratings for the quality of the generated questions (with multiple raters). The criteria associated with the ratings are described in InstructionsParticipants_QGfromSentences.pdf and QGSTEC_INLG2010Proceedings.pdf.


Also included in this folder is the XSLT file used to translate the ratings into a table (evaluation.xsl). The table was exported to MS Excel to calculate various results. There is a spreadsheet (TestDataSummary.xls) which provides a view on the test data and results.


Example:
----------------------------------------------------------------------------------
<dataset>
<instance id="1">
	<id>OpenLearn</id>
	<source>A103_3</source>
	<text>The view that prevailed was that there should be a fitting public memorial to the 49,076 gunners who died.</text>
	<targetQuestionType>how many</targetQuestionType>
	<submission id="a">
		<question type="how many">To the how many gunners who died was the idea that prevailed there should be a public fitting memorial?
		<rating rater="ss" relevance="1" questionType="1" correctness="4" ambiguity="1" variety="2" />
        <rating rater="ha" relevance="1" questionType="1" correctness="1" ambiguity="1" variety="2" />
		</question>
		<question type="how many">The how many gunners who died was the idea that prevailed there should be a public memorial fitting to?
		<rating rater="ss" relevance="4" questionType="1" correctness="4" ambiguity="1" variety="2" />
        <rating rater="ha" relevance="1" questionType="1" correctness="2" ambiguity="1" variety="2" /> 
		</question>
	</submission>
</instance>
----------------------------------------------------------------------------------


ACKNOWLEDGEMENTS
================

We are grateful to a number of people who contributed to the success of Task B of the First Shared Task Evaluation Challenge on Question Generation: Vasile Rus, Rodney Nielsen, Amanda Stent, Arthur Graesser, Jose Otero, and James Lester. The participating teams at the Universities of Lethbridge, Saarland and DFKI, Jadavpur and Wolverhampton kindly gave permission to use the results of their systems and provided rating data. Also, we would like to thank the Engineering and Physical Sciences Research Council who partially supported the effort on Task B through grant EP/G020981/1 (awarded to Paul Piwek). 


CONTACT INFORMATION
===================

Brendan Wyse <bjwyse@gmail.com>
Paul Piwek <p.piwek@open.ac.uk> (Centre for Research in Computing, The Open University, UK)
Svetlana Stoyanchev <s.stoyanchev.ac.uk> (Centre for Research in Computing, The Open University, UK)
