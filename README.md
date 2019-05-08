Following application does Hate Speech Detection on a Mobile Coding Platform.
The backend utilizes the Flask framework with a machine learning model pickeled for prediction of hate speech in posts. The Mobile application communicates with the backend server to process posts and return harmful posts to the administrator.


INSTRUCTIONS:

1. make sure python 3.x is installed
2. do a pip install flask(please refer to this link for flaks help : https://blog.miguelgrinberg.com/post/the-flask-mega-tutorial-part-i-hello-world)
3. do a pip install flask-sqlalchemy and other flask db required packages
4. make sure FLASK_APP environment variable is set to app.py if not running
5. do a flask run to make sure server is running
6. download ngrok https://ngrok.com/download and do ./ngrok http 5000
7. copy the url retrieved into the MainActivity.class static url -> nograkUrl 
8. rebuild the apk 
9. you can now run it.. have fun!


Here is the demo video: 

[![Demo](https://img.youtube.com/vi/O7L3svto5Cw/0.jpg)](https://www.youtube.com/watch?v=O7L3svto5Cw)
