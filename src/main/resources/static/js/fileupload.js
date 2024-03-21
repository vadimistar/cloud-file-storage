const KB = 1024;
const MB = 1024*1024;
const MAX_FILE_SIZE = 500 * MB;

function getSize(size) {
    if (size < KB) {
        return `${size}B`;
    }
    if (size < MB) {
        return `${(size / KB).toFixed(1)}KB`
    }
    return `${(size / MB).toFixed(1)}MB`
}

function removeFile(fileInput, fileIndex) {
    const inputFiles = Array.from(fileInput.files);
    const newFileList = new DataTransfer();
    inputFiles.splice(fileIndex, 1);
    inputFiles.forEach(item => newFileList.items.add(item));
    fileInput.files = newFileList.files;
    const event = new Event('change');
    fileInput.dispatchEvent(event);
}

let filesElements = document.getElementsByName("files");

filesElements.forEach(fileInput => {
    const fileForm = fileInput.parentElement;
    const fileList = fileInput.nextElementSibling;
    const submitButton = fileForm.querySelector('button[type="submit"]');

    fileInput.addEventListener('change', function() {
        if (this.files.length > 0) {
            fileList.removeAttribute('hidden');
        } else {
            fileList.setAttribute('hidden', true);
        }

        let totalSize = 0;
        fileList.innerHTML = '<li></li>'; // Only header element inside

        for (let i = 0; i < this.files.length; i ++) {
            const file = this.files[i];
            const li = document.createElement('li');
            li.className = 'list-group-item align-middle';
            fileList.appendChild(li);

            const removeButton = document.createElement('button');
            removeButton.type = 'button';
            removeButton.className = 'btn-close';
            removeButton.onclick = (event => removeFile(fileInput, i));
            li.appendChild(removeButton);

            const filename = document.createElement('span');
            filename.textContent = file.name;
            li.appendChild(filename);

            const filesize = document.createElement('span');
            filesize.textContent = ' ' + getSize(file.size);
            filesize.className = 'fst-italic';
            li.appendChild(filesize);

            totalSize += file.size;
        }

        const header = fileList.querySelector('li');

        header.className = 'list-group-item ';
        if (totalSize > MAX_FILE_SIZE) {
            header.className += 'list-group-item-danger';
            submitButton.className = 'btn btn-primary w-100 disabled';
        } else {
            header.className += 'list-group-item-primary';
            submitButton.className = 'btn btn-primary w-100';
        }

        header.innerHTML = '';
        const totalSizeSpan = document.createElement('span');
        totalSizeSpan.textContent = `Total size: ${getSize(totalSize)}/${getSize(MAX_FILE_SIZE)}`;
        header.appendChild(totalSizeSpan);
    });
});
