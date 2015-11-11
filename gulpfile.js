// Include gulp
var gulp = require('gulp');

// Include Our Plugins
var sass = require('gulp-sass');
var concatCSS = require('gulp-concat-css');
var minifyCSS = require('gulp-minify-css');
var rename = require('gulp-rename');

// Compile Our Sass
gulp.task('sass', function() {
    return gulp.src('./public/stylesheets/scss/*.scss')
        .pipe(sass())
        .pipe(concatCSS('styles.css'))
        .pipe(gulp.dest('./public/stylesheets/dist'))
        .pipe(rename('style.min.css'))
        .pipe(minifyCSS())
        .pipe(gulp.dest('./public/stylesheets/dist'));
});

// Watch Files For Changes
gulp.task('watch', function() {
    gulp.watch('./public/stylesheets/scss/*.scss', ['sass']);
});

// Default Task
gulp.task('default', ['sass', 'watch']);