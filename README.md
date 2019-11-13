# MoviesShow2
 
This app interacts with a REST API from <a href="https://www.themoviedb.org/">The movie database</a> webiste, fetching popular movies in main activity, retrieving movie details when click an entry and search for movies, this app follows <strong>MVVM Architecture</strong>, this app is continuation of <a href="https://github.com/Abdulrhmman/MovieShows">this app</a>, the app i have built but without local database caching, in this app i have created local database using <strong>room</strong>, saving data in local database and using <strong>glide caching</strong> to save images, also i have improved the code further more following android best practices by adding <strong>NetworkBoundResource</strong>, <strong>Resource</strong> and <strong>ApiResponse</strong> classes.

<h3>Note:</h3>
In order to use this app you will need to go to <a href="https://www.themoviedb.org/account/signup">create account</a> and get your own api key, then put it inside constants file in API_KEY variable.
