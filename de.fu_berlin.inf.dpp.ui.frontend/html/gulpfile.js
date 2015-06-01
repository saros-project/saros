var gulp = require('gulp');
var source = require('vinyl-source-stream');
var browserify = require('browserify');
var templatizer = require('templatizer');
var gutils = require('gulp-util');

var path = {
    app:            './js/app.js',
    templates:      './templates/*.jade',
    watchedJS:      './js/**/*.js',
    jsBundleDir:    './bundle/',
    jsBundle:       'bundle.js',
    templateBundle: './js/templates.js'
};

gulp.task('browserify', ['compile-templates'], function() {

    var bundle = browserify({
        entries: [path.app]
    }).bundle();

    return bundle.on('error', function(err) {
            console.log(err.message);
            gutils.beep();
            // See http://stackoverflow.com/questions/21602332/catching-gulp-mocha-errors
            this.emit('end');
        })
        // Pass desired output filename to vinyl-source-stream
        .pipe(source(path.jsBundle))
        .pipe(gulp.dest(path.jsBundleDir));
});

gulp.task('compile-templates', function() {

    return templatizer(path.templates, path.templateBundle);
});

gulp.task('watch', function() {

    gulp.watch([path.watchedJS, path.templates, '!' + path.templateBundle], ['browserify']);
});
