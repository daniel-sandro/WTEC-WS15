// Include gulp
var gulp = require('gulp');

// Include Our Plugins
var sass = require('gulp-sass');
var concat = require('gulp-concat');
var minifyCss = require('gulp-minify-css');

// Compile Our Sass
gulp.task('sass', function() {
    return gulp.src('public/stylesheets/scss/*.scss')
        .pipe(sass())
        .pipe(concat('styles.css'))
        .pipe(gulp.dest('public/stylesheets/dist'));
});

// Minify CSS
gulp.task('minify', function() {
    return gulp.src('public/stylesheets/dist/styles.css')
        .pipe(minifyCss())
        .pipe(concat('styles.min.css'))
        .pipe(gulp.dest('public/stylesheets/dist'));
});

// Watch Files For Changes
gulp.task('watch', function() {
    gulp.watch('public/stylesheets/scss/*.scss', ['sass', 'minify']);
});

// Default Task
gulp.task('default', ['sass', 'minify', 'watch']);