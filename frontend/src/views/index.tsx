export default function Index() {
    return (
        <div>
            <div>
                <a href="/student/home">Домашняя страница ученика</a>
            </div>
            <div>
                <a href="/student/diary">Страница дневник ученика</a>
            </div>
            <div>
                <a href="/student/grades">Страница оценок ученика</a>
            </div>
            <br />
            <div>
                <a href="/teacher/journal">Страница успеваемости учеников для учителя</a>
            </div>
            <div>
                <a href="/teacher/homework">Страница с дз для учителя</a>
            </div>
            <br />
            <div>
                <a href="/admin/subject">Модификация предметов админ</a>
            </div>
            <div>
                <a href="/admin/period">Модификация четвертей админ</a>
            </div>
            <div>
                <a href="/admin/user">Модификация пользователей админ</a>
            </div>
            <div>
                <a href="/admin/schedule">Модификация расписания админ</a>
            </div>
        </div>

    );
}